package cr.ac.una.smartroute.business;

import cr.ac.una.smartroute.data.entity.Lugar;
import java.time.LocalTime;

/**
 * Una parada ya validada (el lugar existe, está activo, y es compatible con el transporte del
 * usuario) y en el orden en que {@link ItinerarioFactory} debe armarla.
 */
record ParadaPlanificada(Lugar lugar, LocalTime horaInicio, LocalTime horaFin) {
}
