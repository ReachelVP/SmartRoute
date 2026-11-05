package cr.ac.una.smartroute.presentation.dto;

import cr.ac.una.smartroute.data.entity.Lugar;
import java.math.BigDecimal;

public record LugarResponse(
        Long id,
        String nombre,
        String categoria,
        String provincia,
        BigDecimal costoEstimado,
        String nivelAccesibilidad,
        boolean requiereTransportePropio,
        // Laboratorio 7: el cliente debe devolver esta misma versión en el PUT para poder
        // detectar bloqueo optimista (ver LugarAdminService.actualizar).
        Long version) {

    public static LugarResponse from(Lugar lugar) {
        return new LugarResponse(
                lugar.getId(),
                lugar.getNombre(),
                lugar.getCategoria().getNombre(),
                lugar.getProvincia().getNombre(),
                lugar.getCostoEstimado(),
                lugar.getNivelAccesibilidad(),
                lugar.isRequiereTransportePropio(),
                lugar.getVersion());
    }
}
