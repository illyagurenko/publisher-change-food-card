package ru.itone.illya4gurenko.publisher_change_food_card;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.File;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.FileStatus;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class MultipartTest extends BaseIntegrationTest{

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("success multipart upload")
    void testMultipartSuccess() {
        String fileName = "Z001002.GPB_ENROLL8.298";
        ResponseEntity<String> response = sendMultipart(fileName, createValidEnrollFileContent());

        assertEquals(HttpStatus.OK, response.getStatusCode());

        File file = fileRepository.findAll().stream()
                .filter(f -> f.getFilename().equals(fileName))
                .findFirst()
                .orElse(null);

        assertNotNull(file);
        assertEquals(FileStatus.SUCCESS, file.getFileStatus());
        assertEquals(1, gruVistaTabRepository.count());
    }

    @Test
    @DisplayName("error file is exist")
    void testMultipartAlreadyExists() {
        String fileName = "Z001002.GPB_ENROLL9.298";
        sendMultipart(fileName, createValidEnrollFileContent());
        ResponseEntity<String> duplicateResponse = sendMultipart(fileName, createValidEnrollFileContent());
        assertNotEquals(HttpStatus.OK, duplicateResponse.getStatusCode());
    }

    private ResponseEntity<String> sendMultipart(String fileName, String content) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return fileName;
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return restTemplate.postForEntity("/api/files/upload", new HttpEntity<>(body, headers), String.class);
    }
}
