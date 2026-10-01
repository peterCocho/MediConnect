package com.sena.backend.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Clase base para pruebas de integración con una base de datos PostgreSQL
 * real levantada en Docker (no H2).
 *
 * IMPORTANTE: @SpringBootTest va aquí, en la clase abstracta. Spring
 * encuentra esta anotación recorriendo la jerarquía de clases (no depende
 * de @Inherited de Java), así que las subclases NO necesitan repetirla.
 * Si falta, el contexto de Spring nunca arranca y los @Autowired de la
 * subclase quedan en null sin lanzar ningún error hasta que los uses
 * (síntoma típico: NullPointerException en un repositorio dentro de un
 * @BeforeEach).
 *
 * @ActiveProfiles("test") activa application-test.yml (JWT de prueba, n8n y
 * Evolution API mockeados, Flyway deshabilitado, ddl-auto: create → Hibernate
 * arma el esquema directo desde las entidades contra el contenedor).
 *
 * webEnvironment = MOCK (el valor por defecto de @SpringBootTest, aquí
 * explícito): arma un contexto "tipo web" simulado, sin levantar un
 * servidor real ni ocupar un puerto. Es necesario para que Spring Security
 * autoconfigure por completo, incluido el bean AuthenticationConfiguration
 * que SecurityConfig.authenticationManager() necesita. Con WebEnvironment.NONE
 * esa autoconfiguración se omite entera (queda condicionada a
 * @ConditionalOnWebApplication) y el contexto falla con
 * UnsatisfiedDependencyException al no encontrar AuthenticationConfiguration.
 *
 * El contenedor se comparte entre todas las clases que extienden esta base
 * dentro de la misma corrida de mvn test (Testcontainers lo reutiliza
 * mientras la JVM del test siga viva).
 *
 * Requiere Docker Desktop corriendo.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
}