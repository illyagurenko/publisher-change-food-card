package ru.itone.illya4gurenko.publisher_change_food_card.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;


@Service
public class WatchFilesService {
    private final ValidDataService validDataService;

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
    //если он всему подходит то переименовать в ин прогресс и в другом сервисе вызывая проверку строк
    // раскидываем их в ошиьку и успех
    public void findAllFilesAndRename(Path dir) {
        int co = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, validDataService::isFileEnrollFilter)) {
            for (Path pathFile : stream) {
                try{
                    String newName = pathFile.getFileName().toString() + ".in-progress";
                    Path newPath = pathFile.resolveSibling(newName);
                    Files.move(pathFile, newPath);
                    co++;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println(co);
        } catch (IOException | DirectoryIteratorException x) {
            throw new RuntimeException();
        }
    }
}
