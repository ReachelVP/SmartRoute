package cr.ac.una.smartroute.presentation.v1;

import cr.ac.una.smartroute.business.ConfirmarItinerarioService;
import cr.ac.una.smartroute.business.ItinerarioConsultaService;
import cr.ac.una.smartroute.config.security.UsuarioAutenticado;
import cr.ac.una.smartroute.data.entity.Itinerario;
import cr.ac.una.smartroute.presentation.dto.ConfirmarItinerarioRequest;
import cr.ac.una.smartroute.presentation.dto.ItinerarioResponse;
import cr.ac.una.smartroute.presentation.v1.dto.ConfirmarItinerarioV1Request;
import cr.ac.una.smartroute.presentation.v1.dto.PaginaResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Proceso 2 (confirmar) + lecturas. Todo requiere autenticación; la lista y el detalle verifican
 * propiedad del recurso en {@link ItinerarioConsultaService} — un USUARIO solo ve los suyos, un
 * ADMIN ve cualquiera (y puede filtrar explícitamente por {@code usuarioId}).
 */
@RestController
@RequestMapping("/api/v1/itinerarios")
public class ItinerarioV1Controller {

    private final ConfirmarItinerarioService confirmarItinerarioService;
    private final ItinerarioConsultaService itinerarioConsultaService;

    public ItinerarioV1Controller(
            ConfirmarItinerarioService confirmarItinerarioService, ItinerarioConsultaService itinerarioConsultaService) {
        this.confirmarItinerarioService = confirmarItinerarioService;
        this.itinerarioConsultaService = itinerarioConsultaService;
    }

    @PostMapping
    public ResponseEntity<ItinerarioResponse> confirmar(@Valid @RequestBody ConfirmarItinerarioV1Request request) {
        ConfirmarItinerarioRequest comando = new ConfirmarItinerarioRequest(
                UsuarioAutenticado.id(), request.fecha(), request.paradas(), request.confirmarExcesoPresupuesto());
        Itinerario creado = confirmarItinerarioService.confirmar(comando);

        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(ItinerarioResponse.from(creado));
    }

    @GetMapping
    public PaginaResponse<ItinerarioResponse> listar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @PageableDefault(size = 20, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Itinerario> pagina = itinerarioConsultaService.listarPaginado(
                UsuarioAutenticado.id(), UsuarioAutenticado.esAdmin(), usuarioId, estado, fechaDesde, fechaHasta, pageable);
        return PaginaResponse.from(pagina.map(ItinerarioResponse::from));
    }

    @GetMapping("/{id}")
    public ItinerarioResponse obtener(@PathVariable Long id) {
        Itinerario itinerario =
                itinerarioConsultaService.obtener(id, UsuarioAutenticado.id(), UsuarioAutenticado.esAdmin());
        return ItinerarioResponse.from(itinerario);
    }
}
