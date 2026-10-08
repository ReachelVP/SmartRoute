package cr.ac.una.smartroute.data.repository;

import cr.ac.una.smartroute.data.entity.Usuario;
import java.util.Optional;

public interface UsuarioRepository extends BaseRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);
}
