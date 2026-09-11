package cr.ac.una.smartroute.data.specification;

import cr.ac.una.smartroute.data.entity.Itinerario;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * Consulta de negocio (Specification, 2 de 2): "mis itinerarios" con filtros opcionales de estado
 * y rango de fechas — la pantalla real siempre deja elegir cuáles aplicar, así que una consulta
 * JPQL fija con parámetros obligatorios no alcanza.
 */
public final class ItinerarioSpecifications {

    private ItinerarioSpecifications() {
    }

    public static Specification<Itinerario> conFiltrosOpcionales(
            Long usuarioId, String estado, LocalDate fechaDesde, LocalDate fechaHasta) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (usuarioId != null) {
                predicados.add(criteriaBuilder.equal(root.get("usuario").get("id"), usuarioId));
            }
            if (estado != null) {
                predicados.add(criteriaBuilder.equal(root.get("estado"), estado));
            }
            if (fechaDesde != null) {
                predicados.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fecha"), fechaDesde));
            }
            if (fechaHasta != null) {
                predicados.add(criteriaBuilder.lessThanOrEqualTo(root.get("fecha"), fechaHasta));
            }

            return criteriaBuilder.and(predicados.toArray(new Predicate[0]));
        };
    }
}
