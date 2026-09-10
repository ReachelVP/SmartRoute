package cr.ac.una.smartroute.presentation;

import cr.ac.una.smartroute.business.LugarService;
import cr.ac.una.smartroute.presentation.dto.LugarResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lugares")
public class LugarController {

    private final LugarService lugarService;

    public LugarController(LugarService lugarService) {
        this.lugarService = lugarService;
    }

    @GetMapping
    public List<LugarResponse> listar(@RequestParam Long provinciaId) {
        return lugarService.listarPorProvincia(provinciaId).stream().map(LugarResponse::from).toList();
    }
}
