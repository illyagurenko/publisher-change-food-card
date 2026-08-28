package ru.itone.illya4gurenko.publisher_change_food_card.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class GenerateDirService {

    @Value("${spring.files.data:./data}")
    private String dir;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public Path createDir() throws IOException {
        String today = LocalDate.now().format(DATE_FORMAT);
        Path newDir = Paths.get(dir, today);
        Files.createDirectories(newDir.resolve("in_progress"));
        Files.createDirectories(newDir.resolve("success"));
        Files.createDirectories(newDir.resolve("error"));
        return newDir;
    }

    public Path copyToInProgress(Path path) throws IOException {
        Path todayDir = createDir();
        String originalName = path.getFileName().toString();
        Path targetPath = todayDir.resolve("in_progress").resolve(originalName + ".in_progress");

        Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath;
    }

    public Path moveToSuccess(Path inProgressPath, String fileName) throws IOException {
        Path todayDir = createDir();
        Path successPath = todayDir.resolve("success").resolve(fileName + ".success");
        Files.move(inProgressPath, successPath, StandardCopyOption.REPLACE_EXISTING);
        return successPath;
    }

    public Path moveToError(Path inProgressPath, String fileName) throws IOException {
        Path todayDir = createDir();
        Path errorPath = todayDir.resolve("error").resolve(fileName + ".error");
        Files.move(inProgressPath, errorPath, StandardCopyOption.REPLACE_EXISTING);
        return errorPath;
    }
}
