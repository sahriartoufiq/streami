package com.datastream.infrastructure.persistence;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for all persistence integration tests.
 *
 * <p>Starts a single shared PostgreSQL container per test run (reused across subclasses
 * via the static {@code @Container} field). Schema is created by Liquibase on first
 * context load. Each test method runs in a transaction that is rolled back on completion,
 * ensuring full data isolation without repeated schema setup.
 */
@SpringBootTest(properties = "grpc.server.port=-1")
@ActiveProfiles("test")
@Testcontainers
@Transactional
public abstract class AbstractIntegrationTest {

    @Container
    @SuppressWarnings("resource") // lifecycle managed by @Container / Testcontainers
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("datastream")
                    .withUsername("datastream")
                    .withPassword("datastream");

    /**
     * Overrides the datasource properties from {@code application-test.yml} with the
     * explicit container's connection details so that Liquibase and JPA use the same
     * Testcontainers-managed instance.
     *
     * @param registry the property registry provided by Spring's test infrastructure
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }
}
