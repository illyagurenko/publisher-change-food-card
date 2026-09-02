package ru.itone.illya4gurenko.publisher_change_food_card;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.itone.illya4gurenko.grpc.FileChunk;
import ru.itone.illya4gurenko.grpc.FileUploadServiceGrpc;
import ru.itone.illya4gurenko.grpc.UploadStatus;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.File;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.FileStatus;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class GrpcTest extends BaseIntegrationTest {

    private ManagedChannel channel;
    private FileUploadServiceGrpc.FileUploadServiceStub stub;

    @BeforeEach
    void setupGrpc() {
        channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        stub = FileUploadServiceGrpc.newStub(channel);
    }

    @AfterEach
    void tearDownGrpc() {
        if (channel != null) {
            channel.shutdownNow();
        }
    }

    @Test
    @DisplayName("success grpc upload")
    void testGrpcSuccess() throws InterruptedException {
        String fileName = "Z001002.GPB_ENROLL1.298";
        UploadStatus status = sendGrpcFile(fileName, createValidEnrollFileContent());

        assertNotNull(status);
        assertTrue(status.getIsSuccess());

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
    void testGrpcAlreadyExists() throws InterruptedException {
        String fileName = "Z001002.GPB_ENROLL7.298";

        UploadStatus firstStatus = sendGrpcFile(fileName, createValidEnrollFileContent());
        assertTrue(firstStatus.getIsSuccess());

        UploadStatus duplicateStatus = sendGrpcFile(fileName, createValidEnrollFileContent());
        assertFalse(duplicateStatus.getIsSuccess(), "duplicate with error");
    }

    private UploadStatus sendGrpcFile(String fileName, String content) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<UploadStatus> responseRef = new AtomicReference<>();

        StreamObserver<UploadStatus> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(UploadStatus value) {
                responseRef.set(value);
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        StreamObserver<FileChunk> requestObserver = stub.uploadFile(responseObserver);

        requestObserver.onNext(FileChunk.newBuilder()
                .setFileName(fileName)
                .setContent(ByteString.copyFrom(content.getBytes(StandardCharsets.UTF_8)))
                .build());

        requestObserver.onCompleted();
        latch.await(5, TimeUnit.SECONDS);

        return responseRef.get();
    }
}
