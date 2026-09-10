package cr.ac.una.smartroute.presentation.dto;

import cr.ac.una.smartroute.data.entity.Lugar;
import java.util.List;

/**
 * Si {@code criterioLimitante} viene con valor, {@code lugares} viene vacío: significa que ningún
 * lugar cumplió todos los filtros, e indica cuál de ellos fue el que dejó la lista en cero (en vez
 * de devolver una lista vacía sin más contexto, como pide el Proceso 1 del dominio).
 */
public record RecomendacionResponse(List<LugarResponse> lugares, String criterioLimitante) {

    public static RecomendacionResponse exitosa(List<Lugar> lugares) {
        return new RecomendacionResponse(lugares.stream().map(LugarResponse::from).toList(), null);
    }

    public static RecomendacionResponse sinCoincidencias(String criterioLimitante) {
        return new RecomendacionResponse(List.of(), criterioLimitante);
    }
}
