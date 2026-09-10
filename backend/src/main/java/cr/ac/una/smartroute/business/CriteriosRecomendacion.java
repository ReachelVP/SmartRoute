package cr.ac.una.smartroute.business;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Criterios del Proceso 1 del dominio: categorías de interés del perfil, provincia elegida para el
 * paseo, presupuesto máximo y si el usuario tiene transporte propio.
 */
public record CriteriosRecomendacion(
        Long provinciaId,
        Set<Long> categoriaIds,
        BigDecimal presupuestoMaximo,
        boolean tieneTransportePropio) {
}
