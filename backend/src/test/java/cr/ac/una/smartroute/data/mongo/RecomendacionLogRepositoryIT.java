package cr.ac.una.smartroute.data.mongo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cr.ac.una.smartroute.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Prueba de integración del repositorio Mongo generalizado, contra un Mongo real (Testcontainers). */
@SpringBootTest
class RecomendacionLogRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private RecomendacionLogRepository recomendacionLogRepository;

    @Test
    void guardarYEncontrarRecomendacionLog_roundTripBasico() {
        RecomendacionLog log = new RecomendacionLog();
        log.setUsuarioId(1L);
        log.setFecha(Instant.now());

        RecomendacionLog.Criterios criterios = new RecomendacionLog.Criterios();
        criterios.setProvinciaId(1L);
        criterios.setCategorias(List.of("Cultural"));
        criterios.setPresupuestoMaximo(new BigDecimal("10000.00"));
        criterios.setTieneTransportePropio(false);
        log.setCriterios(criterios);
        log.setLugaresSugeridos(List.of());
        log.setPlanesGenerados(List.of());

        RecomendacionLog guardado = recomendacionLogRepository.save(log);

        Optional<RecomendacionLog> encontrado = recomendacionLogRepository.findById(guardado.getId());
        assertTrue(encontrado.isPresent());
        assertEquals(1L, encontrado.get().getUsuarioId());
        assertEquals("Cultural", encontrado.get().getCriterios().getCategorias().get(0));
    }
}
