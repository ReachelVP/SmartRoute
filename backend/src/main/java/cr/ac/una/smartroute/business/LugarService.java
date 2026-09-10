package cr.ac.una.smartroute.business;

import cr.ac.una.smartroute.data.entity.Lugar;
import cr.ac.una.smartroute.data.repository.LugarRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LugarService {

    private final LugarRepository lugarRepository;

    public LugarService(LugarRepository lugarRepository) {
        this.lugarRepository = lugarRepository;
    }

    public List<Lugar> listarPorProvincia(Long provinciaId) {
        return lugarRepository.findByProvinciaId(provinciaId);
    }
}
