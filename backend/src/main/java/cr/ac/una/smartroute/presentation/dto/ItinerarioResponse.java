package cr.ac.una.smartroute.presentation.dto;

import cr.ac.una.smartroute.data.entity.Itinerario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ItinerarioResponse(
        Long id, LocalDate fecha, String estado, BigDecimal presupuestoTotalEstimado, List<ParadaResponse> paradas) {

    public static ItinerarioResponse from(Itinerario itinerario) {
        return new ItinerarioResponse(
                itinerario.getId(),
                itinerario.getFecha(),
                itinerario.getEstado(),
                itinerario.getPresupuestoTotalEstimado(),
                itinerario.getParadas().stream().map(ParadaResponse::from).toList());
    }
}
