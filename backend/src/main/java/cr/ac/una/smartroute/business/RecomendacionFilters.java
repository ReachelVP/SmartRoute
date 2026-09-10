package cr.ac.una.smartroute.business;

import cr.ac.una.smartroute.data.entity.Lugar;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Los cuatro filtros del Proceso 1 del dominio, como funciones puras sobre una lista en memoria:
 * no tocan la base de datos ni el servidor web, así que se prueban con JUnit simple.
 *
 * <p>{@link #aplicar} los encadena en orden y, si algún paso deja la lista en cero, se detiene ahí
 * y reporta ese filtro como el limitante — nunca devuelve una lista vacía sin decir por qué.
 */
public final class RecomendacionFilters {

    private RecomendacionFilters() {
    }

    public static List<Lugar> porCategoriaDeInteres(List<Lugar> lugares, Set<Long> categoriaIds) {
        return lugares.stream()
                .filter(lugar -> categoriaIds.contains(lugar.getCategoria().getId()))
                .toList();
    }

    public static List<Lugar> porProvincia(List<Lugar> lugares, Long provinciaId) {
        return lugares.stream()
                .filter(lugar -> lugar.getProvincia().getId().equals(provinciaId))
                .toList();
    }

    public static List<Lugar> excluyendoTransporteRequeridoSiNoTiene(
            List<Lugar> lugares, boolean tieneTransportePropio) {
        if (tieneTransportePropio) {
            return lugares;
        }
        return lugares.stream().filter(lugar -> !lugar.isRequiereTransportePropio()).toList();
    }

    public static List<Lugar> dentroDePresupuesto(List<Lugar> lugares, BigDecimal presupuestoMaximo) {
        return lugares.stream()
                .filter(lugar -> lugar.getCostoEstimado().compareTo(presupuestoMaximo) <= 0)
                .toList();
    }

    public static ResultadoRecomendacion aplicar(List<Lugar> catalogo, CriteriosRecomendacion criterios) {
        List<Lugar> porCategoria = porCategoriaDeInteres(catalogo, criterios.categoriaIds());
        if (porCategoria.isEmpty()) {
            return new ResultadoRecomendacion.SinCoincidencias("categorías de interés");
        }

        List<Lugar> porProvincia = porProvincia(porCategoria, criterios.provinciaId());
        if (porProvincia.isEmpty()) {
            return new ResultadoRecomendacion.SinCoincidencias("provincia");
        }

        List<Lugar> porTransporte =
                excluyendoTransporteRequeridoSiNoTiene(porProvincia, criterios.tieneTransportePropio());
        if (porTransporte.isEmpty()) {
            return new ResultadoRecomendacion.SinCoincidencias("transporte propio");
        }

        List<Lugar> porPresupuesto = dentroDePresupuesto(porTransporte, criterios.presupuestoMaximo());
        if (porPresupuesto.isEmpty()) {
            return new ResultadoRecomendacion.SinCoincidencias("presupuesto");
        }

        return new ResultadoRecomendacion.Exitoso(porPresupuesto);
    }
}
