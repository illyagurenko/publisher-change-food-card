package ru.itone.illya4gurenko.publisher_change_food_card.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.itone.illya4gurenko.publisher_change_food_card.config.ConstantsUtils;
import ru.itone.illya4gurenko.publisher_change_food_card.dao.GruDao;
import ru.itone.illya4gurenko.publisher_change_food_card.dao.PomDao;
import ru.itone.illya4gurenko.publisher_change_food_card.exception.FileProcessingException;
import ru.itone.illya4gurenko.publisher_change_food_card.exception.FileValidationException;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.FileStatus;
import ru.itone.illya4gurenko.publisher_change_food_card.service.visitor.EnrollVisitor;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessFileService {

    private final PomDao pomDao;
    private final GruDao gruDao;
    private final GenerateDirService generateDirService;

    public void process(Path path){
        String filename = path.getFileName().toString().replace(ConstantsUtils.POINT_IN_PROGRESS, "");
        if (pomDao.existsByFilename(filename)) {
            throw new FileProcessingException("file already exist");
        }
        Path inProgressPath = null;
        EnrollVisitor visitor = null;

        try{
            //если он с контроллеров
            if (path.getFileName().toString().endsWith(ConstantsUtils.POINT_IN_PROGRESS)) {
                inProgressPath = path;
            } else {
                inProgressPath = generateDirService.moveToInProgress(path);
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
            pomDao.updateFileStatus(visitor.getFileEntity(), FileStatus.SUCCESS, null);
            generateDirService.moveToSuccess(inProgressPath, filename);
        } catch (FileValidationException e) {
            log.warn("Validation failed for file {}: {}", filename, e.getMessage());
            if (visitor != null && visitor.getFileEntity() != null) {
                pomDao.updateFileStatus(visitor.getFileEntity(), FileStatus.ERROR, e.getMessage());
            }
            moveToErrorQuietly(inProgressPath, filename);

        } catch (Exception e) {
            log.error("Technical error processing file {}: {}", filename, e.getMessage(), e);
            if (visitor != null && visitor.getFileEntity() != null) {
                pomDao.updateFileStatus(visitor.getFileEntity(), FileStatus.ERROR, "technical error: " + e.getMessage());
            }
            moveToErrorQuietly(inProgressPath, filename);
        }
    }

    private void moveToErrorQuietly(Path inProgressPath, String filename) {
        if (inProgressPath != null && Files.exists(inProgressPath)) {
            try {
                generateDirService.moveToError(inProgressPath, filename);
            } catch (IOException ex) {
                log.error("Failed to move file to error directory: {}", filename, ex);
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
