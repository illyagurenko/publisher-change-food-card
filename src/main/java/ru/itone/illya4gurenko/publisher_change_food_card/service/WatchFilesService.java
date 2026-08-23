package ru.itone.illya4gurenko.publisher_change_food_card.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

@Service
public class WatchFilesService {
    private final ValidDataService validDataService;

    @Value("${files.dir}")
    private String dirForFiles;

    @Autowired
    public WatchFilesService(ValidDataService validDataService) {
        this.validDataService = validDataService;
    }

    public boolean isFileEmpty(Path path) {
        try {
            return Files.exists(path) && Files.size(path) == 0;
        } catch (Exception e) {
            return true;
        }
    }
    
    public void findAllFiles(Path dir, Consumer<Path> consumer) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, validDataService::isFileEnrollFilter)) {
            for (Path pathFile : stream) {
                consumer.accept(pathFile);
            }
        } catch (IOException | DirectoryIteratorException x) {

            throw new RuntimeException();
        }
    }
}
