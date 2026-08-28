package ru.itone.illya4gurenko.publisher_change_food_card.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.itone.illya4gurenko.publisher_change_food_card.dao.PomDao;
import ru.itone.illya4gurenko.publisher_change_food_card.exception.FileProcessingException;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class CheckDirService {
    private final ProcessFileService processFileService;
    private final PomDao pomDao;

    @Value("${spring.files.dir}")
    private String dir;

    @Scheduled(fixedDelayString = "${spring.files.scan-interval:5000}")
    public void scan() {
        Path dirPath = Paths.get(dir);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            for (Path filePath : stream) {
                if (Files.isDirectory(filePath)) {
                    continue;
                }
                String fileName = filePath.getFileName().toString();

                if (pomDao.existsByFilename(fileName)) {
                    continue;
                }
                processFileService.process(filePath);
            }
        } catch ( IOException e) {
            throw new FileProcessingException("error check dir", e);
        }
    }
}
