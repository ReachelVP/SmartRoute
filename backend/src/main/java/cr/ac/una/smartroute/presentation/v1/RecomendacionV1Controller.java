package cr.ac.una.smartroute.presentation.v1;

import cr.ac.una.smartroute.business.RecomendacionService;
import cr.ac.una.smartroute.business.ResultadoRecomendacion;
import cr.ac.una.smartroute.config.security.UsuarioAutenticado;
import cr.ac.una.smartroute.presentation.dto.RecomendacionResponse;
import cr.ac.una.smartroute.presentation.v1.dto.RecomendacionV1Request;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Proceso 1. Requiere autenticación: el usuario (y por lo tanto su perfil) viene del JWT. */
@RestController
@RequestMapping("/api/v1/recomendaciones")
public class RecomendacionV1Controller {

    private final RecomendacionService recomendacionService;

    public RecomendacionV1Controller(RecomendacionService recomendacionService) {
        this.recomendacionService = recomendacionService;
    }

    @PostMapping
    public RecomendacionResponse recomendar(@Valid @RequestBody RecomendacionV1Request request) {
        ResultadoRecomendacion resultado =
                recomendacionService.recomendar(UsuarioAutenticado.id(), request.provinciaId());

        return switch (resultado) {
            case ResultadoRecomendacion.Exitoso exitoso -> RecomendacionResponse.exitosa(exitoso.lugares());
            case ResultadoRecomendacion.SinCoincidencias sinCoincidencias ->
                    RecomendacionResponse.sinCoincidencias(sinCoincidencias.criterioLimitante());
        };
    }
}
