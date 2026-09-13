package ru.itone.illya4gurenko.publisher_change_food_card.base;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.itone.illya4gurenko.grpc.FileUploadServiceGrpc;
import ru.itone.illya4gurenko.publisher_change_food_card.oracle.repository.GruVistaTabRepository;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.repository.FileRepository;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.repository.UnitErrorRepository;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.repository.UnitRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public abstract class BaseIntegrationTest {

    @TempDir
    public static Path tempFolder;

    public static Path watchDir;
    public static Path dataDir;

    static final PostgreSQLContainer<?> POSTGRES;
    static final OracleContainer ORACLE;
    protected static ManagedChannel grpcChannel;
    protected static FileUploadServiceGrpc.FileUploadServiceStub grpcStub;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("postgres_db")
                .withUsername("user")
                .withPassword("root")
                .withInitScript("schema-pom.sql");
        POSTGRES.start();

        ORACLE = new OracleContainer("gvenzl/oracle-xe:11-slim")
                .withDatabaseName("xe")
                .withUsername("gru")
                .withPassword("root")
                .withInitScript("schema-gru.sql");
        ORACLE.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.postgres.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.postgres.username", POSTGRES::getUsername);
        registry.add("spring.datasource.postgres.password", POSTGRES::getPassword);

        registry.add("spring.datasource.oracle.url", ORACLE::getJdbcUrl);
        registry.add("spring.datasource.oracle.username", ORACLE::getUsername);
        registry.add("spring.datasource.oracle.password", ORACLE::getPassword);

        watchDir = tempFolder.resolve("watch");
        dataDir = tempFolder.resolve("data");
        try {
            Files.createDirectories(watchDir);
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        registry.add("spring.files.dir", () -> watchDir.toAbsolutePath().toString());
        registry.add("spring.files.data", () -> dataDir.toAbsolutePath().toString());
        // Отключаем фоновое авто-сканирование шедулера, чтобы вызывать вручную в изолированном тесте
        registry.add("spring.files.scan-interval", () -> "999999999");
    }

    @Autowired
    protected FileRepository fileRepository;

    @Autowired
    protected UnitRepository unitRepository;

    @Autowired
    protected UnitErrorRepository unitErrorRepository;

    @Autowired
    protected GruVistaTabRepository gruVistaTabRepository;

    @BeforeEach
    void clearDatabaseAndWatchDir() throws IOException {
        // 1. Очищаем БД
        gruVistaTabRepository.deleteAll();
        unitErrorRepository.deleteAll();
        unitRepository.deleteAll();
        fileRepository.deleteAll();

        // 2. Гарантированно очищаем папку watch от остатков файлов предыдущих тестов
        if (watchDir != null && Files.exists(watchDir)) {
            try (var stream = Files.newDirectoryStream(watchDir)) {
                for (Path file : stream) {
                    if (!Files.isDirectory(file)) {
                        Files.deleteIfExists(file);
                    }
                }
            }
        } else if (watchDir != null) {
            Files.createDirectories(watchDir);
        }

        if (dataDir != null) {
            Files.createDirectories(dataDir);
        }
    }

    protected byte[] readTestFile(String filename) throws IOException {
        return new ClassPathResource("files/" + filename).getInputStream().readAllBytes();
    }

    protected static FileUploadServiceGrpc.FileUploadServiceStub getGrpcStub() {
        if (grpcChannel == null || grpcChannel.isShutdown()) {
            grpcChannel = ManagedChannelBuilder.forAddress("localhost", 9090)
                    .usePlaintext()
                    .build();
            grpcStub = FileUploadServiceGrpc.newStub(grpcChannel);
        }
        return grpcStub;
    }

    @AfterAll
    static void tearDown() {
        if (grpcChannel != null && !grpcChannel.isShutdown()) {
            grpcChannel.shutdownNow();
        }
    }
}