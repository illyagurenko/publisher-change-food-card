package ru.itone.illya4gurenko.publisher_change_food_card.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.itone.illya4gurenko.publisher_change_food_card.exception.FileProcessingException;

import java.io.IOException;
import java.nio.file.*;

@Service
public class MainManageFilesService {
    private final FileUnitsCheckVisitorService fileUnitsCheckVisitorService;
    private final WatchFilesService watchFilesService;
    private final ValidDataService validDataService;

    @Value("${spring.files.dir}")
    private String dirFilesForManage;


    public MainManageFilesService(FileUnitsCheckVisitorService fileUnitsCheckVisitorService, WatchFilesService watchFilesService, ValidDataService validDataService) {
        this.fileUnitsCheckVisitorService = fileUnitsCheckVisitorService;
        this.watchFilesService = watchFilesService;
        this.validDataService = validDataService;
    }

    public void manageFiles() {
        Path dir = Paths.get(dirFilesForManage);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path pathFile : stream) {
                //вызвать инпрогресс -> проверить на валидность файл -> вызвать error/success
                if (validDataService.isFileEnrollFilter(pathFile)) {
                    Path inProgressPath = watchFilesService.moveToInProgress(pathFile);
                    try {
                        if (fileUnitsCheckVisitorService.visit(inProgressPath)) {
                            watchFilesService.moveToSuccess(inProgressPath);
                        } else {
                            watchFilesService.moveToError(inProgressPath);                        }
                    } catch (Exception  e) {
                        throw new FileProcessingException("Error with processing file: " + pathFile, e);
                    }
                }
            }
        } catch (IOException | DirectoryIteratorException x) {
            throw new FileProcessingException("Error reading dir: " + dirFilesForManage, x);
        }
    }
}
