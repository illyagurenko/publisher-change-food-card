package ru.itone.illya4gurenko.publisher_change_food_card.grpc;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.itone.illya4gurenko.grpc.FileChunk;
import ru.itone.illya4gurenko.grpc.UploadStatus;
import ru.itone.illya4gurenko.publisher_change_food_card.base.BaseIntegrationTest;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.File;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.FileStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class GrpcTest extends BaseIntegrationTest {

    private UploadStatus sendGrpcChunks(String filename, byte[] data, int chunkSize) throws InterruptedException, IOException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<UploadStatus> statusRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        StreamObserver<FileChunk> requestObserver = getGrpcStub().uploadFile(new StreamObserver<>() {
            @Override
            public void onNext(UploadStatus value) {
                statusRef.set(value);
            }

            @Override
            public void onError(Throwable t) {
                errorRef.set(t);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        byte[] buffer = new byte[chunkSize];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            FileChunk chunk = FileChunk.newBuilder()
                    .setFileName(filename)
                    .setContent(ByteString.copyFrom(buffer, 0, bytesRead))
                    .build();
            requestObserver.onNext(chunk);
        }
        requestObserver.onCompleted();

        boolean completedInTime = latch.await(10, TimeUnit.SECONDS);
        assertTrue(completedInTime, "gRPC stream timed out");
        assertNull(errorRef.get(), "gRPC stream returned error");
        return statusRef.get();
    }

    @Test
    @DisplayName("valid immediate file process success")
    void testGrpcValidImmediate() throws IOException, InterruptedException {
        String filename = "Z001002.VALID_ENROLL2.298";
        byte[] bytes = readTestFile(filename);

        UploadStatus status = sendGrpcChunks(filename, bytes, 64);

        assertTrue(status.getIsSuccess());
        assertEquals(bytes.length, status.getBytesReceived());

        File file = fileRepository.findAll().getFirst();
        assertEquals(FileStatus.SUCCESS, file.getFileStatus());
        assertEquals(1, gruVistaTabRepository.count());
    }

    @Test
    @DisplayName("valid intime file process success")
    void testGrpcValidIntime() throws IOException, InterruptedException {
        String filename = "Z001002.VALIDINTIME_ENROLL2.298";
        byte[] bytes = readTestFile(filename);

        UploadStatus status = sendGrpcChunks(filename, bytes, 64);

        assertTrue(status.getIsSuccess());
        File file = fileRepository.findAll().getFirst();
        assertEquals(FileStatus.SUCCESS, file.getFileStatus());
        assertEquals(2, gruVistaTabRepository.count());
    }

    @Test
    @DisplayName("error body negative amount file process error")
    void testGrpcErrorBody() throws IOException, InterruptedException {
        String filename = "Z001002.ERRORBODY_ENROLL2.298";
        byte[] bytes = readTestFile(filename);

        UploadStatus status = sendGrpcChunks(filename, bytes, 64);

        assertFalse(status.getIsSuccess());
        File file = fileRepository.findAll().getFirst();
        assertEquals(FileStatus.ERROR, file.getFileStatus());
        assertEquals(0, gruVistaTabRepository.count());
    }

    @Test
    @DisplayName("error trailer string format file process error")
    void testGrpcErrorTrailer() throws IOException, InterruptedException {
        String filename = "Z001002.ERRORTRAILER_ENROLL2.298";
        byte[] bytes = readTestFile(filename);

        UploadStatus status = sendGrpcChunks(filename, bytes, 64);

        assertFalse(status.getIsSuccess());
        File file = fileRepository.findAll().getFirst();
        assertEquals(FileStatus.ERROR, file.getFileStatus());
    }
}