package cr.ac.una.smartroute.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * Entrada del Proceso 2 (confirmar itinerario). {@code paradas} viene en el orden en que el
 * usuario piensa visitarlas. {@code confirmarExcesoPresupuesto} es el "salvo que confirme
 * explícitamente que quiere excederlo" de la regla de presupuesto de la propuesta de dominio.
 */
public record ConfirmarItinerarioRequest(
        @NotNull(message = "usuarioId es obligatorio") Long usuarioId,
        @NotNull(message = "fecha es obligatoria") @FutureOrPresent(message = "la fecha no puede ser en el pasado")
                LocalDate fecha,
        @NotEmpty(message = "el itinerario debe tener al menos 1 parada")
                @Size(max = 5, message = "el itinerario admite máximo 5 paradas")
                List<@Valid ParadaConfirmacionRequest> paradas,
        boolean confirmarExcesoPresupuesto) {
}
