package cr.ac.una.smartroute.presentation.v1.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ResenaCreateRequest(
        @NotNull(message = "lugarId es obligatorio") Long lugarId,
        @NotNull(message = "calificacion es obligatoria") @Min(value = 1, message = "calificacion mínima es 1")
                @Max(value = 5, message = "calificacion máxima es 5") Short calificacion,
        String comentario) {
}
