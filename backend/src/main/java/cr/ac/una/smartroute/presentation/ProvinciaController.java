package cr.ac.una.smartroute.presentation;

import cr.ac.una.smartroute.business.ProvinciaService;
import cr.ac.una.smartroute.presentation.dto.ProvinciaResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/provincias")
public class ProvinciaController {

    private final ProvinciaService provinciaService;

    public ProvinciaController(ProvinciaService provinciaService) {
        this.provinciaService = provinciaService;
    }

    @GetMapping
    public List<ProvinciaResponse> listar() {
        return provinciaService.listarTodas().stream().map(ProvinciaResponse::from).toList();
    }
}
