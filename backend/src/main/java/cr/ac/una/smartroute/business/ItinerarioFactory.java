package cr.ac.una.smartroute.business;

import cr.ac.una.smartroute.data.entity.Itinerario;
import cr.ac.una.smartroute.data.entity.ItinerarioParada;
import cr.ac.una.smartroute.data.entity.Usuario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Patrón Factory. Señal que lo justificó: construir un {@code Itinerario} válido no es solo
 * "copiar campos" — hay que asignar el {@code orden} de cada parada según su posición en la lista,
 * fijar {@code costoEstimado} a partir del {@code Lugar} resuelto (no del request) y sumar el
 * presupuesto total. Ese conocimiento no le corresponde ni al servicio (que orquesta reglas y
 * repositorios) ni al controlador; aislarlo aquí evita repetirlo si mañana hay otra forma de crear
 * un itinerario (p. ej. a partir de un plan sugerido por el motor de recomendación).
 */
final class ItinerarioFactory {

    private ItinerarioFactory() {
    }

    static Itinerario crear(Usuario usuario, LocalDate fecha, List<ParadaPlanificada> paradasPlanificadas) {
        Itinerario itinerario = new Itinerario();
        itinerario.setUsuario(usuario);
        itinerario.setFecha(fecha);
        itinerario.setEstado("CONFIRMADO");
        itinerario.setCreadoEn(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;
        short orden = 1;
        for (ParadaPlanificada planificada : paradasPlanificadas) {
            ItinerarioParada parada = new ItinerarioParada();
            parada.setItinerario(itinerario);
            parada.setLugar(planificada.lugar());
            parada.setOrden(orden++);
            parada.setHoraInicio(planificada.horaInicio());
            parada.setHoraFin(planificada.horaFin());
            parada.setCostoEstimado(planificada.lugar().getCostoEstimado());
            itinerario.getParadas().add(parada);
            total = total.add(planificada.lugar().getCostoEstimado());
        }
        itinerario.setPresupuestoTotalEstimado(total);

        return itinerario;
    }
}
