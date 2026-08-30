package ru.itone.illya4gurenko.publisher_change_food_card.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.itone.illya4gurenko.publisher_change_food_card.service.GenerateDirService;
import ru.itone.illya4gurenko.publisher_change_food_card.service.ProcessFileService;

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileMultipartController {
    private final ProcessFileService processFileService;
    private final GenerateDirService generateDirService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadMultipart(@RequestParam("file") MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        Path inProgressPath = generateDirService.readStreamInprogress(file.getInputStream(), filename);

        processFileService.process(inProgressPath);
        return ResponseEntity.ok("File processed successfully: " + filename);
    }
}
