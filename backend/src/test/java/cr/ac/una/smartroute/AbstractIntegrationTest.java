package cr.ac.una.smartroute;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.MongoDBContainer;

/**
 * Base para pruebas de integración contra bases de datos reales. Los contenedores son
 * "singleton": se arrancan una sola vez (bloque estático, no el ciclo de vida por-clase de
 * {@code @Container}) y se comparten entre todas las clases de prueba que extiendan esta, para no
 * pagar el costo de arranque (~15-20s) en cada una. Testcontainers los apaga solo al terminar la
 * JVM (Ryuk); no hace falta detenerlos a mano.
 *
 * <p>Reemplaza a los {@code services:} que el CI usaba antes para SQL Server y Mongo: cada
 * ejecución de pruebas trae su propia base real, sin depender de contenedores pre-provisionados
 * por el workflow ni de {@code docker compose up} corrido de antemano.
 */
public abstract class AbstractIntegrationTest {

    private static final MSSQLServerContainer<?> SQLSERVER =
            new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest").acceptLicense();

    private static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

    static {
        SQLSERVER.start();
        MONGO.start();
    }

    @DynamicPropertySource
    static void propiedadesDeContenedores(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> SQLSERVER.getJdbcUrl() + ";encrypt=true;trustServerCertificate=true");
        registry.add("spring.datasource.username", SQLSERVER::getUsername);
        registry.add("spring.datasource.password", SQLSERVER::getPassword);
        registry.add("spring.mongodb.uri", MONGO::getReplicaSetUrl);
    }
}
