package cr.ac.una.smartroute.business;

import cr.ac.una.smartroute.data.entity.Provincia;
import cr.ac.una.smartroute.data.repository.ProvinciaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProvinciaService {

    private final ProvinciaRepository provinciaRepository;

    public ProvinciaService(ProvinciaRepository provinciaRepository) {
        this.provinciaRepository = provinciaRepository;
    }

    public List<Provincia> listarTodas() {
        return provinciaRepository.findAll();
    }
}
