package ru.itone.illya4gurenko.publisher_change_food_card.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itone.illya4gurenko.publisher_change_food_card.dao.PomDao;
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
    private final PomDao pomDao;

    @PostMapping(value = "/stream")
    public ResponseEntity<String> uploadChunkedStream(
            @RequestHeader(value = "X-File-Name", required = false) String headerFilename,
            @RequestHeader(value = "filename", required = false) String altFilename,
            HttpServletRequest request) throws IOException {

        String filename = headerFilename != null ? headerFilename : altFilename;
        if (pomDao.existsByFilename(filename)) {
            return ResponseEntity.badRequest().body("File " + filename + " already processed");
        }
        if (filename == null || filename.isBlank()) {
            filename = "received_stream_file_" + System.currentTimeMillis();
        }

        Path inProgressPath = generateDirService.readStreamInprogress(request.getInputStream(), filename);
        processFileService.process(inProgressPath);

        return ResponseEntity.ok("Stream file processed successfully: " + filename);
    }
}
