package cr.ac.una.smartroute.business.exception;

import java.math.BigDecimal;

/**
 * Regla del Proceso 2: "el costo total calculado no puede superar el presupuesto máximo del
 * PerfilPreferencias del usuario, salvo que confirme explícitamente que quiere excederlo". Se
 * lanza después de guardar el itinerario y sus paradas — es justo esta excepción la que demuestra
 * que la operación es todo-o-nada: {@code @Transactional} deshace también lo ya escrito.
 */
public class PresupuestoExcedidoException extends RuntimeException {

    public PresupuestoExcedidoException(BigDecimal costoTotal, BigDecimal presupuestoMaximo) {
        super("El costo total (" + costoTotal + ") supera el presupuesto máximo del perfil ("
                + presupuestoMaximo + "). Confirme explícitamente si desea excederlo.");
    }
}
