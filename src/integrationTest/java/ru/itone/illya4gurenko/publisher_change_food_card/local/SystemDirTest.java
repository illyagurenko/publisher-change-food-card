package ru.itone.illya4gurenko.publisher_change_food_card.local;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.itone.illya4gurenko.publisher_change_food_card.base.BaseIntegrationTest;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.File;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.FileStatus;
import ru.itone.illya4gurenko.publisher_change_food_card.service.CheckDirService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SystemDirTest extends BaseIntegrationTest {

    @Autowired
    private CheckDirService checkDirService;

    @Test
    @DisplayName("valid immediate file process success")
    void testScanValidImmediateFile() throws IOException {
        String filename = "Z001002.VALID_ENROLL2.298";
        Files.createDirectories(watchDir);
        Path targetPath = watchDir.resolve(filename);
        Files.write(targetPath, readTestFile(filename));

        checkDirService.scan();

        assertFalse(Files.exists(targetPath), "File should be moved from watch folder");
        File file = fileRepository.findAll().getFirst();
        assertEquals(FileStatus.SUCCESS, file.getFileStatus());
        assertEquals(1, gruVistaTabRepository.count());
    }

    @Test
    @DisplayName("valid intime file process success")
    void testScanValidIntimeFile() throws IOException {
        String filename = "Z001002.VALIDINTIME_ENROLL2.298";
        Files.createDirectories(watchDir);
        Path targetPath = watchDir.resolve(filename);
        Files.write(targetPath, readTestFile(filename));

        checkDirService.scan();

        File file = fileRepository.findAll().getFirst();
        assertEquals(FileStatus.SUCCESS, file.getFileStatus());
        assertEquals(2, gruVistaTabRepository.count());
    }

    @Test
    @DisplayName("error count rows mismatch file process error")
    void testScanErrorCountFile() throws IOException {
        String filename = "Z001002.ERRORCOUNT_ENROLL1.298";
        Path targetPath = watchDir.resolve(filename);
        Files.write(targetPath, readTestFile(filename));

        checkDirService.scan();

        File file = fileRepository.findAll().getFirst();
        assertEquals(FileStatus.ERROR, file.getFileStatus());
        assertEquals(0, gruVistaTabRepository.count());
    }

    @Test
    @DisplayName("error file is exist")
    void testScanSkipAlreadyProcessedFile() throws IOException {
        String filename = "Z001002.VALID_ENROLL2.298";
        Path targetPath = watchDir.resolve(filename);
        Files.write(targetPath, readTestFile(filename));

        checkDirService.scan();
        assertEquals(1, fileRepository.count());

        Files.write(targetPath, readTestFile(filename));
        checkDirService.scan();

        assertEquals(1, fileRepository.count());
    }
}