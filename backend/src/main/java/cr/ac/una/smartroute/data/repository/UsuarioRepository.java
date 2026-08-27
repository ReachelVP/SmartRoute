package cr.ac.una.smartroute.data.repository;

import cr.ac.una.smartroute.data.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
