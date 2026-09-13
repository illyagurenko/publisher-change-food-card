package ru.itone.illya4gurenko.publisher_change_food_card.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import ru.itone.illya4gurenko.publisher_change_food_card.base.BaseIntegrationTest;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.File;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.FileStatus;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChunkedTest extends BaseIntegrationTest {

    private final TestRestTemplate restTemplate = new TestRestTemplate();
    private static final String STREAM_URL = "http://localhost:8081/api/files/stream";

    private ResponseEntity<String> sendStream(String filename, byte[] content) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-File-Name", filename);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return restTemplate.postForEntity(STREAM_URL, new HttpEntity<>(content, headers), String.class);
    }

    @Test
    @DisplayName("valid immediate file process success")
    void testValidStreamUpload() throws IOException {
        String filename = "Z001002.VALID_ENROLL2.298";
        byte[] bytes = readTestFile(filename);

        ResponseEntity<String> response = sendStream(filename, bytes);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        File file = fileRepository.findAll().getFirst();
        assertEquals(FileStatus.SUCCESS, file.getFileStatus());
        assertEquals(1, gruVistaTabRepository.count());
    }

    @Test
    @DisplayName("valid intime file process success")
    void testValidIntimeStreamUpload() throws IOException {
        String filename = "Z001002.VALIDINTIME_ENROLL2.298";
        byte[] bytes = readTestFile(filename);

        ResponseEntity<String> response = sendStream(filename, bytes);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        File file = fileRepository.findAll().getFirst();
        assertEquals(FileStatus.SUCCESS, file.getFileStatus());
        assertEquals(2, gruVistaTabRepository.count());
    }

    @Test
    @DisplayName("error header file process error")
    void testCorruptedHeaderStreamUpload() throws IOException {
        String filename = "Z001002.ERRORHEADER_ENROLL2.298";
        byte[] bytes = readTestFile(filename);

        sendStream(filename, bytes);

        File file = fileRepository.findAll().getFirst();
        assertEquals(FileStatus.ERROR, file.getFileStatus());
    }

    @Test
    @DisplayName("error file is exist")
    void testStreamFileAlreadyExists() throws IOException {
        String filename = "Z001002.VALID_ENROLL2.298";
        byte[] bytes = readTestFile(filename);

        sendStream(filename, bytes);
        ResponseEntity<String> secondResponse = sendStream(filename, bytes);

        assertEquals(HttpStatus.BAD_REQUEST, secondResponse.getStatusCode());
        assertTrue(secondResponse.getBody().contains("already processed"));
    }
}