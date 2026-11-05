package cr.ac.una.smartroute.presentation.v1.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record LugarCreateRequest(
        @NotBlank(message = "nombre es obligatorio") String nombre,
        String descripcion,
        @NotNull(message = "provinciaId es obligatorio") Long provinciaId,
        @NotNull(message = "categoriaId es obligatorio") Long categoriaId,
        @NotNull(message = "costoEstimado es obligatorio") @DecimalMin(value = "0.0", message = "costoEstimado no puede ser negativo")
                BigDecimal costoEstimado,
        @NotNull(message = "nivelAccesibilidad es obligatorio")
                @Pattern(regexp = "ALTA|MEDIA|BAJA", message = "nivelAccesibilidad debe ser ALTA, MEDIA o BAJA")
                String nivelAccesibilidad,
        boolean requiereTransportePropio,
        // Solo obligatorio para PUT (bloqueo optimista, Laboratorio 7) -- el controlador lo valida
        // ahí, no acá, porque POST (crear) todavía no tiene una versión que mandar.
        Long version) {
}
