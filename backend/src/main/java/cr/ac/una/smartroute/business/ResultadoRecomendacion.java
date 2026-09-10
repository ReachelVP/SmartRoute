package cr.ac.una.smartroute.business;

import cr.ac.una.smartroute.data.entity.Lugar;
import java.util.List;

/**
 * Resultado de aplicar {@link RecomendacionFilters}. Nunca es una lista vacía sin explicación: si
 * ningún lugar sobrevive los filtros, {@link SinCoincidencias} dice cuál de los cuatro criterios
 * fue el que limitó demasiado (validación explícita del Proceso 1 del dominio).
 */
public sealed interface ResultadoRecomendacion {

    record Exitoso(List<Lugar> lugares) implements ResultadoRecomendacion {
    }

    record SinCoincidencias(String criterioLimitante) implements ResultadoRecomendacion {
    }
}
