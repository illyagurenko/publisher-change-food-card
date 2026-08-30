package ru.itone.illya4gurenko.publisher_change_food_card.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itone.illya4gurenko.publisher_change_food_card.dao.GruDao;
import ru.itone.illya4gurenko.publisher_change_food_card.dao.PomDao;
import ru.itone.illya4gurenko.publisher_change_food_card.exception.FileProcessingException;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.FileStatus;
import ru.itone.illya4gurenko.publisher_change_food_card.service.visitor.EnrollVisitor;
import ru.itone.illya4gurenko.publisher_change_food_card.service.visitor.Visitor;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
// как вынести виситор в абстракцию -> добавить в интерфейс метод hasError и сделать фабрику
@Service
@RequiredArgsConstructor
public class ProcessFileService {

    private final PomDao pomDao;
    private final GruDao gruDao;
    private final GenerateDirService generateDirService;

    public void process(Path path){
        String filename = path.getFileName().toString().replace(".in_progress", "");
        if (pomDao.existsByFilename(filename)) {
            throw new FileProcessingException("file already exist");
        }
        Path inProgressPath = null;
        EnrollVisitor visitor = null;

        try{
            //если он с контроллеров
            if (path.getFileName().toString().endsWith(".in_progress")) {
                inProgressPath = path;
            } else {
                inProgressPath = generateDirService.copyToInProgress(path);
            }
            String fullPathDir = inProgressPath.getParent().toAbsolutePath().toString();
            String lastLine = readLastLine(inProgressPath);
            if (lastLine == null || lastLine.isBlank()) {
                throw new FileProcessingException("file or trailer empty");
            }
            visitor = new EnrollVisitor(pomDao, gruDao,lastLine, fullPathDir, filename);
            try (Stream<String> lines = Files.lines(inProgressPath, StandardCharsets.UTF_8)) {
                lines.forEach(visitor::visit);
            }
            if (visitor.isHasFileError()) {
                if (visitor.getFileEntity() != null) {
                    pomDao.updateFileStatus(visitor.getFileEntity(), FileStatus.ERROR, "error validation");
                }
                generateDirService.moveToError(inProgressPath, filename);
            } else {
                pomDao.updateFileStatus(visitor.getFileEntity(), FileStatus.SUCCESS, null);
                generateDirService.moveToSuccess(inProgressPath, filename);
            }
        } catch (Exception  e) {
            if (visitor != null && visitor.getFileEntity() != null) {
                pomDao.updateFileStatus(visitor.getFileEntity(), FileStatus.ERROR, "technic error: " + e.getMessage());
            }

            if (inProgressPath != null && Files.exists(inProgressPath)) {
                try {
                    generateDirService.moveToError(inProgressPath, filename);
                } catch (Exception ex) {
                    throw new FileProcessingException("fatal error moving files");
                }
            }
        }
    }

    private String readLastLine(Path path) throws IOException {
        try (RandomAccessFile fileHandler = new RandomAccessFile(path.toFile(), "r")) {
            long fileLength = fileHandler.length() - 1;
            if (fileLength < 0) {
                return null;
            }

            StringBuilder sb = new StringBuilder();
            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) { // символ \n
                    if (filePointer == fileLength) {
                        continue; // пропускаем завершающий перенос строки
                    }
                    break;
                } else if (readByte == 0xD) { // символ \r
                    if (filePointer == fileLength - 1) {
                        continue;
                    }
                    break;
                }
                sb.append((char) readByte);
            }
            return sb.reverse().toString();
        }
    }
}
