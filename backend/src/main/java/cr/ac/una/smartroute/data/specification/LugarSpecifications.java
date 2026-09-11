package cr.ac.una.smartroute.data.specification;

import cr.ac.una.smartroute.data.entity.Lugar;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * Consulta de negocio (Specification, 1 de 2): búsqueda dinámica de lugares para el catálogo
 * administrable (Lab 1, dentro del alcance) — todos los filtros son opcionales, a diferencia de
 * {@code RecomendacionFilters} (business), que exige los cuatro criterios completos para el motor
 * de recomendación. Son dos casos de uso distintos, no dos formas de hacer lo mismo.
 */
public final class LugarSpecifications {

    private LugarSpecifications() {
    }

    public static Specification<Lugar> conFiltrosOpcionales(
            Long provinciaId, Long categoriaId, BigDecimal presupuestoMaximo, Boolean soloSinTransporteRequerido) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicados = new ArrayList<>();
            predicados.add(criteriaBuilder.isTrue(root.get("activo")));

            if (provinciaId != null) {
                predicados.add(criteriaBuilder.equal(root.get("provincia").get("id"), provinciaId));
            }
            if (categoriaId != null) {
                predicados.add(criteriaBuilder.equal(root.get("categoria").get("id"), categoriaId));
            }
            if (presupuestoMaximo != null) {
                predicados.add(criteriaBuilder.lessThanOrEqualTo(root.get("costoEstimado"), presupuestoMaximo));
            }
            if (Boolean.TRUE.equals(soloSinTransporteRequerido)) {
                predicados.add(criteriaBuilder.isFalse(root.get("requiereTransportePropio")));
            }

            return criteriaBuilder.and(predicados.toArray(new Predicate[0]));
        };
    }
}
