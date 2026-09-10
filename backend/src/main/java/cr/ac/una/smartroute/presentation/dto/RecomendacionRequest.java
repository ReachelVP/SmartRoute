package cr.ac.una.smartroute.presentation.dto;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Criterios que envía el cliente para pedir una recomendación (Proceso 1 del dominio).
 */
public record RecomendacionRequest(
        Long provinciaId,
        Set<Long> categoriaIds,
        BigDecimal presupuestoMaximo,
        boolean tieneTransportePropio) {
}
