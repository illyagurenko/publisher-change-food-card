package ru.itone.illya4gurenko.publisher_change_food_card.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.itone.illya4gurenko.publisher_change_food_card.dao.PomDao;
import ru.itone.illya4gurenko.publisher_change_food_card.service.GenerateDirService;
import ru.itone.illya4gurenko.publisher_change_food_card.service.ProcessFileService;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileMultipartController {
    private final ProcessFileService processFileService;
    private final GenerateDirService generateDirService;
    private final PomDao pomDao;

    @PostMapping(value = "/upload",  consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadMultipart(@RequestParam("file") MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (pomDao.existsByFilename(filename)) {
            log.warn("file: {} is exist in db", filename);
            return ResponseEntity.badRequest().body("File '" + filename + "' already processed");
        }
        if (file.isEmpty() || filename == null || filename.isBlank()) {
            log.warn("empty name");
            return ResponseEntity.badRequest().body("file is empty or filename");
        }
        Path inProgressPath = generateDirService.readStreamInprogress(file.getInputStream(), filename);
        processFileService.process(inProgressPath);
        log.info("file: {} success multipart get", filename);
        return ResponseEntity.ok("File processed successfully: " + filename);
    }
}
