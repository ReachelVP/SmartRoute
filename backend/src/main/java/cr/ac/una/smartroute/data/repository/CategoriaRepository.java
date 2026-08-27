package cr.ac.una.smartroute.data.repository;

import cr.ac.una.smartroute.data.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
