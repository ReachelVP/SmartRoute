package cr.ac.una.smartroute.data.specification;

import cr.ac.una.smartroute.data.entity.Resena;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** Consulta de negocio (Specification): listar reseñas, opcionalmente filtradas por lugar o usuario. */
public final class ResenaSpecifications {

    private ResenaSpecifications() {
    }

    public static Specification<Resena> conFiltrosOpcionales(Long lugarId, Long usuarioId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicados = new ArrayList<>();
            if (lugarId != null) {
                predicados.add(criteriaBuilder.equal(root.get("lugar").get("id"), lugarId));
            }
            if (usuarioId != null) {
                predicados.add(criteriaBuilder.equal(root.get("usuario").get("id"), usuarioId));
            }
            return criteriaBuilder.and(predicados.toArray(new Predicate[0]));
        };
    }
}
