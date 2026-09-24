package cr.ac.una.smartroute.presentation.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Pide una recomendación para un usuario (Proceso 1 del dominio). Las categorías de interés, el
 * presupuesto máximo y si tiene transporte propio se leen del {@code PerfilPreferencias} del
 * usuario — no se reciben en la petición; solo la provincia es "por paseo", como describe la
 * propuesta de dominio.
 */
public record RecomendacionRequest(
        @NotNull(message = "usuarioId es obligatorio") Long usuarioId,
        @NotNull(message = "provinciaId es obligatorio") Long provinciaId) {
}
