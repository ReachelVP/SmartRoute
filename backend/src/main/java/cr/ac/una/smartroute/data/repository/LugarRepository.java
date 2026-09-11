package cr.ac.una.smartroute.data.repository;

import cr.ac.una.smartroute.data.entity.Lugar;
import java.util.List;

public interface LugarRepository extends BaseRepository<Lugar, Long> {

    List<Lugar> findByProvinciaId(Long provinciaId);
}
