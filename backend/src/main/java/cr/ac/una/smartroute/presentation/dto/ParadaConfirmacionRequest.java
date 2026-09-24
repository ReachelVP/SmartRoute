package cr.ac.una.smartroute.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record ParadaConfirmacionRequest(
        @NotNull(message = "lugarId es obligatorio") Long lugarId,
        @NotNull(message = "horaInicio es obligatoria") LocalTime horaInicio,
        @NotNull(message = "horaFin es obligatoria") LocalTime horaFin) {
}
