package cr.ac.una.smartroute.presentation;

import cr.ac.una.smartroute.business.UsuarioService;
import cr.ac.una.smartroute.presentation.dto.UsuarioResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listarTodos().stream().map(UsuarioResponse::from).toList();
    }
}
