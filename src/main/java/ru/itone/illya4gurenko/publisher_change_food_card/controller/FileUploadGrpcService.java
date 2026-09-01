package ru.itone.illya4gurenko.publisher_change_food_card.controller;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.itone.illya4gurenko.grpc.FileChunk;
import ru.itone.illya4gurenko.grpc.FileUploadServiceGrpc;
import ru.itone.illya4gurenko.grpc.UploadStatus;
import ru.itone.illya4gurenko.publisher_change_food_card.config.ConstantsUtils;
import ru.itone.illya4gurenko.publisher_change_food_card.service.GenerateDirService;
import ru.itone.illya4gurenko.publisher_change_food_card.service.ProcessFileService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class FileUploadGrpcService extends FileUploadServiceGrpc.FileUploadServiceImplBase {

    private final ProcessFileService processFileService;
    private final GenerateDirService generateDirService;

    @Override
    public StreamObserver<FileChunk> uploadFile(StreamObserver<UploadStatus> responseObserver) {
        return new StreamObserver<FileChunk>() {

            private String filename;
            private Path inProgressPath;
            private OutputStream outputStream;
            private long totalBytesReceived = 0;

            // вызывается когда приходит чанк(поток байтов)
            @Override
            public void onNext(FileChunk fileChunk) {
                try{
                    if(outputStream == null){
                        filename = fileChunk.getFileName();
                        log.info("get grpc-chunk for file: {}", filename);

                        Path todayDir = generateDirService.createDir();
                        inProgressPath = todayDir.resolve(ConstantsUtils.DIR_IN_PROGRESS)
                                .resolve(filename + ConstantsUtils.POINT_IN_PROGRESS);
                        outputStream = Files.newOutputStream(inProgressPath);
                    }

                    byte[] bytes = fileChunk.getContent().toByteArray();
                    outputStream.write(bytes);
                    totalBytesReceived += bytes.length;

                } catch (IOException e) {
                    log.error("error write chunk");
                    onError(e);
                }

            }

            // если оборвано соединение
            @Override
            public void onError(Throwable t) {
                log.error("error grpc process file '{}': {}", filename, t.getMessage());
                closeStreamQuietly();

                UploadStatus status = UploadStatus.newBuilder()
                        .setIsSuccess(false)
                        .setMessage("Upload failed: " + t.getMessage())
                        .setBytesReceived(totalBytesReceived)
                        .build();

                responseObserver.onNext(status);
                responseObserver.onCompleted();
            }

            // все чанки переданы
            @Override
            public void onCompleted() {
                log.info("gRPC stream end. get {} byte for file '{}'", totalBytesReceived, filename);
                closeStreamQuietly();

                try {
                    processFileService.process(inProgressPath);

                    UploadStatus status = UploadStatus.newBuilder()
                            .setIsSuccess(true)
                            .setMessage("file processed successfully")
                            .setBytesReceived(totalBytesReceived)
                            .build();

                    responseObserver.onNext(status);
                    responseObserver.onCompleted();

                } catch (Exception e) {
                    log.error("error process file: {}", filename, e);

                    UploadStatus status = UploadStatus.newBuilder()
                            .setIsSuccess(false)
                            .setMessage("processing error: " + e.getMessage())
                            .setBytesReceived(totalBytesReceived)
                            .build();

                    responseObserver.onNext(status);
                    responseObserver.onCompleted();
                }
            }

            private void closeStreamQuietly() {
                if (outputStream != null) {
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (Exception e) {
                        log.error("error input stream", e);
                    }
                }
            }
        };
    }
}
