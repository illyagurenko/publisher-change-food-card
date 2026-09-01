package ru.itone.illya4gurenko.publisher_change_food_card.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.itone.illya4gurenko.publisher_change_food_card.config.ConstantsUtils;

import java.io.IOException;
import java.io.InputStream;
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
        Files.createDirectories(newDir.resolve(ConstantsUtils.DIR_IN_PROGRESS));
        Files.createDirectories(newDir.resolve(ConstantsUtils.DIR_SUCCESS));
        Files.createDirectories(newDir.resolve(ConstantsUtils.DIR_ERROR));
        return newDir;
    }

    public Path moveToInProgress(Path path) throws IOException {
        Path todayDir = createDir();
        String originalName = path.getFileName().toString();
        Path targetPath = todayDir.resolve(ConstantsUtils.DIR_IN_PROGRESS).resolve(originalName + ConstantsUtils.POINT_IN_PROGRESS);

        Files.move(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath;
    }

    public Path moveToSuccess(Path inProgressPath, String fileName) throws IOException {
        Path todayDir = createDir();
        Path successPath = todayDir.resolve(ConstantsUtils.DIR_SUCCESS).resolve(fileName + ConstantsUtils.POINT_SUCCESS);
        Files.move(inProgressPath, successPath, StandardCopyOption.REPLACE_EXISTING);
        return successPath;
    }

    public Path moveToError(Path inProgressPath, String fileName) throws IOException {
        Path todayDir = createDir();
        Path errorPath = todayDir.resolve(ConstantsUtils.DIR_ERROR).resolve(fileName + ConstantsUtils.POINT_ERROR);
        Files.move(inProgressPath, errorPath, StandardCopyOption.REPLACE_EXISTING);
        return errorPath;
    }

    // принимает поток чанки и пишет в ин-прогресс
    public Path readStreamInprogress(InputStream inputStream, String fileName) throws IOException {
        Path todayDir = createDir();
        Path targetPath = todayDir.resolve(ConstantsUtils.DIR_IN_PROGRESS).resolve(fileName + ConstantsUtils.POINT_IN_PROGRESS);
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath;
    }
}
