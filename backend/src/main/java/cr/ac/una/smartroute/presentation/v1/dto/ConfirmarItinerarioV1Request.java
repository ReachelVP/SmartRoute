package cr.ac.una.smartroute.presentation.v1.dto;

import cr.ac.una.smartroute.presentation.dto.ParadaConfirmacionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/** A diferencia de la versión heredada, {@code usuarioId} no viaja en el cuerpo: viene del JWT. */
public record ConfirmarItinerarioV1Request(
        @NotNull(message = "fecha es obligatoria") @FutureOrPresent(message = "la fecha no puede ser en el pasado")
                LocalDate fecha,
        @NotEmpty(message = "el itinerario debe tener al menos 1 parada")
                @Size(max = 5, message = "el itinerario admite máximo 5 paradas")
                List<@Valid ParadaConfirmacionRequest> paradas,
        boolean confirmarExcesoPresupuesto) {
}
