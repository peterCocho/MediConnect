package com.sena.backend.integration;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Clase base para pruebas de integración con una base de datos PostgreSQL
 * real levantada en Docker (no H2). Las clases de test extienden esta clase
 * y quedan con el contenedor + @ServiceConnection ya configurados: Spring
 * Boot conecta automáticamente el DataSource al contenedor, sin tocar
 * application.yml ni definir spring.datasource.url a mano.
 *
 * El contenedor se comparte entre TODAS las clases que extienden esta base
 * dentro de la misma corrida de mvn test (Testcontainers reutiliza el mismo
 * contenedor mientras el JVM del test siga viva), así que Flyway solo migra
 * una vez y las clases no compiten por levantar contenedores por separado.
 *
 * Requiere Docker Desktop corriendo.
 */
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
}