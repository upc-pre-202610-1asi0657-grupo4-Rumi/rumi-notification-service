package com.rumi.notification.integration;

import com.rumi.notification.NotificationServiceApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Starts the service against a real PostgreSQL. The tables come from docs/schema.sql and Hibernate only
 * validates them, as in Docker Compose, so a mismatch between the schema and the entities fails here.
 */
@CucumberContextConfiguration
@SpringBootTest(
        classes = NotificationServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class NotificationIntegrationTestConfiguration {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("notificationsdb")
            .withUsername("rumi")
            .withPassword("rumi");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.schema-locations", () -> "file:docs/schema.sql");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @LocalServerPort
    int port;

    final TestRestTemplate http = new TestRestTemplate();

    public NotificationIntegrationTestConfiguration() {
        // The JDK client supports PATCH, which acknowledge and resolve use.
        http.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    }

    String url(String path) {
        return "http://localhost:" + port + path;
    }
}
