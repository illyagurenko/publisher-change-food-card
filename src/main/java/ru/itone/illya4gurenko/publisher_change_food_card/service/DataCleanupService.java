package ru.itone.illya4gurenko.publisher_change_food_card.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class DataCleanupService {

    @Value("${spring.files.data:./data}")
    private String dataBasePath;

    @Value("${spring.files.retention-days:3}")
    private int retentionDays;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Scheduled(cron = "${spring.files.cleanup-cron:0 0 1 * * ?}")
    public void cleanupOldDirectories() {
        Path baseDir = Paths.get(dataBasePath);

        if (!Files.exists(baseDir) || !Files.isDirectory(baseDir)) {
            return;
        }

        LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);
        log.info("start clean dir {} days ago", cutoffDate.format(DATE_FORMAT));

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseDir)) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    continue;
                }

                String folderName = path.getFileName().toString();

                if (folderName.matches("^\\d{8}$")) {
                    try {
                        LocalDate folderDate = LocalDate.parse(folderName, DATE_FORMAT);

                        if (folderDate.isBefore(cutoffDate)) {
                            log.info("delete old data: {}", path);
                            boolean deleted = FileSystemUtils.deleteRecursively(path);
                            if (deleted) {
                                log.info("dir {} success delete", folderName);
                            } else {
                                log.warn("error delete {}", folderName);
                            }
                        }
                    } catch (DateTimeParseException e) {
                        log.debug("dir {} not correct", folderName);
                    }
                }
            }
        } catch (IOException e) {
            log.error("tech error: {}", dataBasePath, e);
        }
    }
}
