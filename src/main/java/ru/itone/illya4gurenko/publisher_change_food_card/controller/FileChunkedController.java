package ru.itone.illya4gurenko.publisher_change_food_card.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itone.illya4gurenko.publisher_change_food_card.service.GenerateDirService;
import ru.itone.illya4gurenko.publisher_change_food_card.service.ProcessFileService;

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileChunkedController {
    private final ProcessFileService processFileService;
    private final GenerateDirService generateDirService;

    @PostMapping(value = "/stream", consumes = "application/octet-stream")
    public ResponseEntity<String> uploadChunkedStream(
            @RequestHeader("X-File-Name") String filename,
            HttpServletRequest request) throws IOException {

        Path inProgressPath = generateDirService.readStreamInprogress(request.getInputStream(), filename);

        processFileService.process(inProgressPath);
        return ResponseEntity.ok("Stream file processed successfully: " + filename);
    }
}
