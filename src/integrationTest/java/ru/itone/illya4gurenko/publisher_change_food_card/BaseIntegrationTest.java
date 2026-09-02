package ru.itone.illya4gurenko.publisher_change_food_card;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.itone.illya4gurenko.publisher_change_food_card.oracle.repository.GruVistaTabRepository;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.repository.FileRepository;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.repository.UnitErrorRepository;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.repository.UnitRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public abstract class BaseIntegrationTest {
    
    static final PostgreSQLContainer<?> POSTGRES;
    static final OracleContainer ORACLE;

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
    void clearDatabase() {
        gruVistaTabRepository.deleteAll();
        unitErrorRepository.deleteAll();
        unitRepository.deleteAll();
        fileRepository.deleteAll();
    }

    protected String createValidEnrollFileContent() {
        String header = "H 20231025 120000 IMMEDIATE\n";
        String body = String.format("%-100s%-30s%2s%20s\n", "Петров Петр Петрович", "0000000000000000", "ZR", "1500.50");
        String trailer = String.format("%1s%9s%10s", "T", " ", "1");
        return header + body + trailer;
    }
}