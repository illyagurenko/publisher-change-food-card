package ru.itone.illya4gurenko.publisher_change_food_card.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;

@Service
public class MainManageFilesService {
    private final FileUnitsCheckVisitorService fileUnitsCheckVisitorService;
    private final WatchFilesService watchFilesService;

    @Value("${spring.files.dir}")
    private String dirFilesForManage;

    @Value("${spring.files.error}")
    private String dirForFilesError;

    @Value("${spring.files.success}")
    private String dirForFilesSuccess;

    public MainManageFilesService(FileUnitsCheckVisitorService fileUnitsCheckVisitorService, WatchFilesService watchFilesService) {
        this.fileUnitsCheckVisitorService = fileUnitsCheckVisitorService;
        this.watchFilesService = watchFilesService;
    }

    public void manageFiles(){
        Path dir = Paths.get(dirFilesForManage);
        watchFilesService.findAllFilesAndRename(dir);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path pathFile : stream) {
                try{
                    if(fileUnitsCheckVisitorService.visit(pathFile)){
                        Path newDir = Paths.get(dirForFilesSuccess);
                        Files.createDirectories(newDir);
                        String newName = pathFile.getFileName().toString().replace(".in-progress", ".success");
                        Path newFile = newDir.resolve(newName);
                        Files.copy(pathFile, newFile, StandardCopyOption.REPLACE_EXISTING);
                    }else{
                        Path newDir = Paths.get(dirForFilesError);
                        Files.createDirectories(newDir);
                        String newName = pathFile.getFileName().toString().replace(".in-progress", ".error");
                        Path newFile = newDir.resolve(newName);
                        Files.copy(pathFile, newFile, StandardCopyOption.REPLACE_EXISTING);
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException | DirectoryIteratorException x) {
            throw new RuntimeException();
        }
    }
}
