package ru.itone.illya4gurenko.publisher_change_food_card.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.itone.illya4gurenko.publisher_change_food_card.base.BaseIntegrationTest;
import ru.itone.illya4gurenko.publisher_change_food_card.config.ConstantsUtils;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.File;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.FileStatus;
import ru.itone.illya4gurenko.publisher_change_food_card.service.visitor.dto.ProcType;
import ru.itone.illya4gurenko.publisher_change_food_card.service.visitor.dto.Type;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MultipartTest extends BaseIntegrationTest {

    private final TestRestTemplate restTemplate = new TestRestTemplate();
    private static final String UPLOAD_URL = "http://localhost:8081/api/files/upload";

    private ResponseEntity<String> sendMultipart(String filename, byte[] content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource resource = new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
        body.add("file", resource);

        return restTemplate.postForEntity(UPLOAD_URL, new HttpEntity<>(body, headers), String.class);
    }

    @Test
    @DisplayName("valid immediate file process success")
    void testValidImmediateFile() throws IOException {
        String filename = "Z001002.VALID_ENROLL2.298";
        byte[] bytes = readTestFile(filename);

        ResponseEntity<String> response = sendMultipart(filename, bytes);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        File file = fileRepository.findAll().getFirst();
        assertEquals(FileStatus.SUCCESS, file.getFileStatus());
        assertEquals("001 002", file.getSender());
        assertEquals("298", file.getUliDate());

        assertEquals(1, gruVistaTabRepository.count());
        var gru = gruVistaTabRepository.findAll().getFirst();
        assertEquals(new BigDecimal("1500.500"), gru.getXalfa().setScale(3));
        assertEquals(Type.ZR, gru.getOperation());
        assertEquals(ProcType.IMMEDIATE, gru.getFocType());
        assertNull(gru.getFocTS());
    }

    @Test
    @DisplayName("valid intime file process success")
    void testValidIntimeFile() throws IOException {
        String filename = "Z001002.VALIDINTIME_ENROLL2.298";
        byte[] bytes = readTestFile(filename);

        ResponseEntity<String> response = sendMultipart(filename, bytes);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        File file = fileRepository.findAll().getFirst();
        assertEquals(FileStatus.SUCCESS, file.getFileStatus());

        assertEquals(2, gruVistaTabRepository.count());
        var records = gruVistaTabRepository.findAll();
        assertTrue(records.stream().allMatch(r -> r.getFocType() == ProcType.INTIME));
        assertTrue(records.stream().allMatch(r -> r.getFocTS() != null));
    }

    @Test
    @DisplayName("error header file process error")
    void testErrorHeader() throws IOException {
        String filename = "Z001002.ERRORHEADER_ENROLL2.298";
        byte[] bytes = readTestFile(filename);

        sendMultipart(filename, bytes);

        File file = fileRepository.findAll().getFirst();
        assertEquals(FileStatus.ERROR, file.getFileStatus());
        assertTrue(unitErrorRepository.findAll().stream().anyMatch(e -> ConstantsUtils.ERR_CODE_HEADER.equals(e.getErrorCode())));
        assertEquals(0, gruVistaTabRepository.count());
    }

    @Test
    @DisplayName("error body negative amount file process error")
    void testErrorBodyNegativeAmount() throws IOException {
        String filename = "Z001002.ERRORBODY_ENROLL2.298";
        byte[] bytes = readTestFile(filename);

        sendMultipart(filename, bytes);

        File file = fileRepository.findAll().getFirst();
        assertEquals(FileStatus.ERROR, file.getFileStatus());
        assertTrue(unitErrorRepository.findAll().stream().anyMatch(e -> ConstantsUtils.ERR_CODE_NEGATIVE_AMOUNT.equals(e.getErrorCode())));
        assertEquals(0, gruVistaTabRepository.count());
    }

    @Test
    @DisplayName("error count rows mismatch file process error")
    void testErrorCountRows() throws IOException {
        String filename = "Z001002.ERRORCOUNT_ENROLL1.298";
        byte[] bytes = readTestFile(filename);

        sendMultipart(filename, bytes);

        File file = fileRepository.findAll().getFirst();
        assertEquals(FileStatus.ERROR, file.getFileStatus());
        assertTrue(unitErrorRepository.findAll().stream().anyMatch(e -> ConstantsUtils.ERR_CODE_TRAILER.equals(e.getErrorCode())));
    }

    @Test
    @DisplayName("error trailer string format file process error")
    void testErrorTrailerFormat() throws IOException {
        String filename = "Z001002.ERRORTRAILER_ENROLL2.298";
        byte[] bytes = readTestFile(filename);

        sendMultipart(filename, bytes);

        File file = fileRepository.findAll().getFirst();
        assertEquals(FileStatus.ERROR, file.getFileStatus());
        assertTrue(unitErrorRepository.findAll().stream().anyMatch(e -> ConstantsUtils.ERR_CODE_TRAILER.equals(e.getErrorCode())));
    }

    @Test
    @DisplayName("error file is exist")
    void testFileAlreadyExists() throws IOException {
        String filename = "Z001002.VALID_ENROLL2.298";
        byte[] bytes = readTestFile(filename);

        ResponseEntity<String> firstResponse = sendMultipart(filename, bytes);
        assertEquals(HttpStatus.OK, firstResponse.getStatusCode());

        ResponseEntity<String> secondResponse = sendMultipart(filename, bytes);
        assertEquals(HttpStatus.BAD_REQUEST, secondResponse.getStatusCode());
        assertTrue(secondResponse.getBody().contains("already processed"));
    }
}