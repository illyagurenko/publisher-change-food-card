package ru.itone.illya4gurenko.publisher_change_food_card;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.File;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.FileStatus;
import ru.itone.illya4gurenko.publisher_change_food_card.service.CheckDirService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class SystemDirTest extends BaseIntegrationTest {

    @Autowired
    private CheckDirService checkDirService;

    @Value("${spring.files.dir}")
    private String sourceDir;

    @Test
    @DisplayName("success process file")
    void testSystemFileProcessingSuccess() throws IOException {
        String fileName = "Z001002.GPB_ENROLL1.298";
        createFileInSourceDir(fileName, createValidEnrollFileContent());

        checkDirService.scan();

        File file = findFileByName(fileName);
        assertNotNull(file);
        assertEquals(FileStatus.SUCCESS, file.getFileStatus());
        assertNull(file.getFileComment(), "must be null");

        assertEquals(1, gruVistaTabRepository.count(), "1 rows in gru");

        assertEquals(0, unitErrorRepository.count(), "0 rows in error table");
    }

    @Test
    @DisplayName("error count rows in trailer")
    void testSystemFileProcessingErrorCount() throws IOException{
        String fileName = "Z001002.GPB_ENROLL2.298";
        String header = "H 20231025 120000 IMMEDIATE\n";
        String body = String.format("%-100s%-30s%2s%20s\n", "Петров Петр Петрович", "0000000000000000", "ZR", "1500.50");
        String trailer = String.format("%1s%9s%10s", "T", " ", "5");

        createFileInSourceDir(fileName, header + body + trailer);

        checkDirService.scan();

        File file = findFileByName(fileName);
        assertNotNull(file);
        assertEquals(FileStatus.ERROR, file.getFileStatus());
        assertNotNull(file.getFileComment(), "musn`t be null error");

        assertTrue(unitErrorRepository.count() > 0, "1+ rows in error table");
    }

    @Test
    @DisplayName("error invalid header")
    void testSystemFileProcessingErrorHeader() throws IOException {
        String fileName = "Z001002.GPB_ENROLL3.298";
        String header = "H hhhhhh\n";
        String body = String.format("%-100s%-30s%2s%20s\n", "Петров Петр Петрович", "0000000000000000", "ZR", "1500.50");
        String trailer = String.format("%1s%9s%10s", "T", " ", "1");

        createFileInSourceDir(fileName, header + body + trailer);

        checkDirService.scan();

        File file = findFileByName(fileName);
        assertNotNull(file);
        assertEquals(FileStatus.ERROR, file.getFileStatus());
        assertTrue(unitErrorRepository.count() > 0, "1+ rows in error table");
    }

    @Test
    @DisplayName("error body negative amount")
    void testSystemFileProcessingErrorBodyAmount() throws IOException {
        String fileName = "Z001002.GPB_ENROLL4.298";
        String header = "H 20231025 120000 IMMEDIATE\n";
        String body = String.format("%-100s%-30s%2s%20s\n", "Петров Петр Петрович", "0000000000000000", "ZR", "-1500.50");
        String trailer = String.format("%1s%9s%10s", "T", " ", "1");

        createFileInSourceDir(fileName, header + body + trailer);

        checkDirService.scan();

        File file = findFileByName(fileName);
        assertNotNull(file);
        assertEquals(FileStatus.ERROR, file.getFileStatus());

        assertEquals(0, gruVistaTabRepository.count());

        boolean hasAmountError = unitErrorRepository.findAll().stream()
                .anyMatch(err -> "B05".equals(err.getErrorCode()));
        assertTrue(hasAmountError, "В POM.UNIT_ERROR must be error B05");
    }


    private void createFileInSourceDir(String fileName, String content) throws IOException {
        Path targetFile = Paths.get(sourceDir, fileName);
        Files.createDirectories(targetFile.getParent());
        Files.writeString(targetFile, content, StandardCharsets.UTF_8);
    }

    private File findFileByName(String fileName) {
        return fileRepository.findAll().stream()
                .filter(f -> f.getFilename().equals(fileName))
                .findFirst()
                .orElse(null);
    }
}
