package cr.ac.una.smartroute.business;

import cr.ac.una.smartroute.data.entity.Lugar;
import cr.ac.una.smartroute.data.repository.LugarRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Orquesta el Proceso 1 del dominio: trae el catálogo de lugares activos (dato real) y le aplica
 * los filtros puros de {@link RecomendacionFilters} (regla de negocio, sin dato real de por medio).
 */
@Service
public class RecomendacionService {

    private final LugarRepository lugarRepository;

    public RecomendacionService(LugarRepository lugarRepository) {
        this.lugarRepository = lugarRepository;
    }

    public ResultadoRecomendacion recomendar(CriteriosRecomendacion criterios) {
        List<Lugar> catalogo = lugarRepository.findAll().stream().filter(Lugar::isActivo).toList();
        return RecomendacionFilters.aplicar(catalogo, criterios);
    }
}
