package cr.ac.una.smartroute.presentation.v1;

import cr.ac.una.smartroute.business.ResenaService;
import cr.ac.una.smartroute.config.security.UsuarioAutenticado;
import cr.ac.una.smartroute.data.entity.Resena;
import cr.ac.una.smartroute.data.specification.ResenaSpecifications;
import cr.ac.una.smartroute.presentation.v1.dto.PaginaResponse;
import cr.ac.una.smartroute.presentation.v1.dto.ResenaCreateRequest;
import cr.ac.una.smartroute.presentation.v1.dto.ResenaResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Lectura pública; crear una reseña requiere estar autenticado (el usuario sale del JWT). */
@RestController
@RequestMapping("/api/v1/resenas")
public class ResenaV1Controller {

    private final ResenaService resenaService;

    public ResenaV1Controller(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    @GetMapping
    public PaginaResponse<ResenaResponse> listar(
            @RequestParam(required = false) Long lugarId,
            @PageableDefault(size = 20, sort = "creadoEn", direction = Sort.Direction.DESC) Pageable pageable) {
        var filtro = ResenaSpecifications.conFiltrosOpcionales(lugarId, null);
        Page<Resena> pagina = resenaService.listarPaginado(filtro, pageable);
        return PaginaResponse.from(pagina.map(ResenaResponse::from));
    }

    @GetMapping("/{id}")
    public ResenaResponse obtener(@PathVariable Long id) {
        return ResenaResponse.from(resenaService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ResenaResponse> crear(@Valid @RequestBody ResenaCreateRequest request) {
        Resena creada = resenaService.crear(
                UsuarioAutenticado.id(), request.lugarId(), request.calificacion(), request.comentario());

        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.getId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(ResenaResponse.from(creada));
    }
}
