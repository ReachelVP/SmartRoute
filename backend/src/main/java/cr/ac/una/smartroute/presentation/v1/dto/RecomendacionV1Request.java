package cr.ac.una.smartroute.presentation.v1.dto;

import jakarta.validation.constraints.NotNull;

/** A diferencia de la versión heredada, {@code usuarioId} no viaja en el cuerpo: viene del JWT. */
public record RecomendacionV1Request(@NotNull(message = "provinciaId es obligatorio") Long provinciaId) {
}
