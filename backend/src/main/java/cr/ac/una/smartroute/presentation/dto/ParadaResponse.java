package cr.ac.una.smartroute.presentation.dto;

import cr.ac.una.smartroute.data.entity.ItinerarioParada;
import java.math.BigDecimal;
import java.time.LocalTime;

public record ParadaResponse(
        Long lugarId, String nombreLugar, int orden, LocalTime horaInicio, LocalTime horaFin, BigDecimal costoEstimado) {

    public static ParadaResponse from(ItinerarioParada parada) {
        return new ParadaResponse(
                parada.getLugar().getId(),
                parada.getLugar().getNombre(),
                parada.getOrden(),
                parada.getHoraInicio(),
                parada.getHoraFin(),
                parada.getCostoEstimado());
    }
}
