package cr.ac.una.smartroute.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cr.ac.una.smartroute.AbstractIntegrationTest;
import cr.ac.una.smartroute.business.exception.PresupuestoExcedidoException;
import cr.ac.una.smartroute.data.repository.ItinerarioRepository;
import cr.ac.una.smartroute.presentation.dto.ConfirmarItinerarioRequest;
import cr.ac.una.smartroute.presentation.dto.ParadaConfirmacionRequest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de integración (Testcontainers, SQL Server real) de la garantía transaccional del
 * Proceso 2: provoca un fallo intermedio (presupuesto excedido, detectado DESPUÉS de guardar el
 * itinerario y sus paradas) y verifica que ninguna escritura quedó persistida — la prueba central
 * del Laboratorio 4.
 *
 * <p>Usuario 2 (Kimberley, seed V2) tiene transporte propio y presupuesto máximo 35000.00. Volcán
 * Poás (8000) + Catarata La Paz (15000) + Parque Nacional Manuel Antonio (18000) suman 41000.00 —
 * supera el presupuesto sin que el usuario lo haya confirmado.
 */
@SpringBootTest
class ConfirmarItinerarioServiceRollbackIT extends AbstractIntegrationTest {

    private static final Long USUARIO_KIMBERLEY = 2L;
    private static final LocalDate FECHA_PRUEBA = LocalDate.now().plusDays(30);

    @Autowired
    private ConfirmarItinerarioService confirmarItinerarioService;

    @Autowired
    private ItinerarioRepository itinerarioRepository;

    @Test
    void confirmar_deshaceTodoLoEscrito_siElPresupuestoSeExcedeSinConfirmarlo() {
        ConfirmarItinerarioRequest request = new ConfirmarItinerarioRequest(
                USUARIO_KIMBERLEY,
                FECHA_PRUEBA,
                List.of(
                        new ParadaConfirmacionRequest(9L, LocalTime.of(8, 0), LocalTime.of(12, 0)), // Manuel Antonio
                        new ParadaConfirmacionRequest(1L, LocalTime.of(12, 30), LocalTime.of(14, 30)), // Volcán Poás
                        new ParadaConfirmacionRequest(2L, LocalTime.of(15, 0), LocalTime.of(17, 0))), // Catarata La Paz
                false);

        assertThrows(PresupuestoExcedidoException.class, () -> confirmarItinerarioService.confirmar(request));

        long itinerariosCreados = itinerarioRepository.findAll().stream()
                .filter(i -> i.getUsuario().getId().equals(USUARIO_KIMBERLEY) && FECHA_PRUEBA.equals(i.getFecha()))
                .count();
        assertEquals(0, itinerariosCreados, "El itinerario no debió quedar persistido tras el rollback");
    }
}
