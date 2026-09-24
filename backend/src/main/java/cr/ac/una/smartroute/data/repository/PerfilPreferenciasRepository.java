package cr.ac.una.smartroute.data.repository;

import cr.ac.una.smartroute.data.entity.PerfilPreferencias;
import java.util.Optional;

public interface PerfilPreferenciasRepository extends BaseRepository<PerfilPreferencias, Long> {

    Optional<PerfilPreferencias> findByUsuarioId(Long usuarioId);
}
