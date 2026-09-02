package ru.itone.illya4gurenko.publisher_change_food_card;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.File;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.FileStatus;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class ChunkedTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("success chunked upload")
    void testChunkedSuccess() {
        String fileName = "Z001002.GPB_ENROLL5.298";
        ResponseEntity<String> response = sendChunkedStream(fileName, createValidEnrollFileContent());

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
    @DisplayName("error chunked without http-header")
    void testChunkedMissingHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        HttpEntity<byte[]> request = new HttpEntity<>(createValidEnrollFileContent().getBytes(StandardCharsets.UTF_8), headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/files/stream", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("error file is exist")
    void testChunkedAlreadyExists() {
        String fileName = "Z001002.GPB_ENROLL6.298";
        sendChunkedStream(fileName, createValidEnrollFileContent());

        ResponseEntity<String> duplicateResponse = sendChunkedStream(fileName, createValidEnrollFileContent());
        assertNotEquals(HttpStatus.OK, duplicateResponse.getStatusCode());
    }

    private ResponseEntity<String> sendChunkedStream(String fileName, String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-File-Name", fileName);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<byte[]> request = new HttpEntity<>(content.getBytes(StandardCharsets.UTF_8), headers);
        return restTemplate.postForEntity("/api/files/stream", request, String.class);
    }
}