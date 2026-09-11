package cr.ac.una.smartroute.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Repositorio base para todas las entidades relacionales. Extender
 * {@link JpaSpecificationExecutor} aquí, y no en cada repositorio específico, es lo que le da a
 * cualquier entidad soporte de Specifications (consultas dinámicas) sin repetir la declaración.
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
}
