package ru.itone.illya4gurenko.publisher_change_food_card.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.itone.illya4gurenko.publisher_change_food_card.exception.FileProcessingException;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Service
public class WatchFilesService {

    @Value("${spring.files.data}")
    private String baseDataDir;

    public Path moveToInProgress(Path pathFile) {
        return moveToState(pathFile, "in_progress", ".in-progress");
    }

    public Path moveToSuccess(Path pathFile) {
        return moveToState(pathFile, "success", ".success");
    }

    public Path moveToError(Path pathFile) {
        return moveToState(pathFile, "error", ".error");
    }

    private Path moveToState(Path pathFile, String folderName, String suffix) {
        try {
            String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

            Path targetDir = Paths.get(baseDataDir, date, folderName);
            Files.createDirectories(targetDir);

            String cleanName = pathFile.getFileName().toString()
                    .replace(".in-progress", "")
                    .replace(".success", "")
                    .replace(".error", "");

            String newFileName = cleanName + suffix;
            Path targetFile = targetDir.resolve(newFileName);

            return Files.move(pathFile, targetFile, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new FileProcessingException("Error move " + pathFile.getFileName() + " in dir " + folderName, e);
        }
    }
}