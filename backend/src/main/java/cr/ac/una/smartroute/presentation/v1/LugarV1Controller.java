package cr.ac.una.smartroute.presentation.v1;

import cr.ac.una.smartroute.business.LugarAdminService;
import cr.ac.una.smartroute.data.entity.Lugar;
import cr.ac.una.smartroute.data.specification.LugarSpecifications;
import cr.ac.una.smartroute.presentation.dto.LugarResponse;
import cr.ac.una.smartroute.presentation.v1.dto.LugarCreateRequest;
import cr.ac.una.smartroute.presentation.v1.dto.PaginaResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Catálogo de lugares. Lectura pública; alta/edición/baja restringidas a ADMIN (ver
 * {@code SecurityConfig}). La baja es lógica ({@code activo = false}) — hay itinerarios y reseñas
 * que referencian el lugar.
 */
@RestController
@RequestMapping("/api/v1/lugares")
public class LugarV1Controller {

    private final LugarAdminService lugarAdminService;

    public LugarV1Controller(LugarAdminService lugarAdminService) {
        this.lugarAdminService = lugarAdminService;
    }

    @GetMapping
    public PaginaResponse<LugarResponse> listar(
            @RequestParam(required = false) Long provinciaId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) BigDecimal presupuestoMaximo,
            @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        var filtro = LugarSpecifications.conFiltrosOpcionales(provinciaId, categoriaId, presupuestoMaximo, null);
        Page<Lugar> pagina = lugarAdminService.listarPaginado(filtro, pageable);
        return PaginaResponse.from(pagina.map(LugarResponse::from));
    }

    @GetMapping("/{id}")
    public LugarResponse obtener(@PathVariable Long id) {
        return LugarResponse.from(lugarAdminService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<LugarResponse> crear(@Valid @RequestBody LugarCreateRequest request) {
        Lugar creado = lugarAdminService.crear(
                request.nombre(),
                request.descripcion(),
                request.provinciaId(),
                request.categoriaId(),
                request.costoEstimado(),
                request.nivelAccesibilidad(),
                request.requiereTransportePropio());

        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(LugarResponse.from(creado));
    }

    @PutMapping("/{id}")
    public LugarResponse actualizar(@PathVariable Long id, @Valid @RequestBody LugarCreateRequest request) {
        if (request.version() == null) {
            // Bloqueo optimista (Laboratorio 7): sin la versión que el cliente leyó, no hay forma
            // de detectar que alguien más editó el lugar mientras tanto.
            throw new IllegalArgumentException("version es obligatorio para actualizar un lugar.");
        }
        Lugar actualizado = lugarAdminService.actualizar(
                id,
                request.nombre(),
                request.descripcion(),
                request.provinciaId(),
                request.categoriaId(),
                request.costoEstimado(),
                request.nivelAccesibilidad(),
                request.requiereTransportePropio(),
                request.version());
        return LugarResponse.from(actualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        lugarAdminService.eliminar(id);
    }
}
