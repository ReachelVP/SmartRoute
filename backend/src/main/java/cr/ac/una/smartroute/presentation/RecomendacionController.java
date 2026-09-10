package cr.ac.una.smartroute.presentation;

import cr.ac.una.smartroute.business.CriteriosRecomendacion;
import cr.ac.una.smartroute.business.RecomendacionService;
import cr.ac.una.smartroute.business.ResultadoRecomendacion;
import cr.ac.una.smartroute.presentation.dto.RecomendacionRequest;
import cr.ac.una.smartroute.presentation.dto.RecomendacionResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recomendaciones")
public class RecomendacionController {

    private final RecomendacionService recomendacionService;

    public RecomendacionController(RecomendacionService recomendacionService) {
        this.recomendacionService = recomendacionService;
    }

    @PostMapping
    public RecomendacionResponse recomendar(@RequestBody RecomendacionRequest request) {
        CriteriosRecomendacion criterios = new CriteriosRecomendacion(
                request.provinciaId(),
                request.categoriaIds(),
                request.presupuestoMaximo(),
                request.tieneTransportePropio());

        ResultadoRecomendacion resultado = recomendacionService.recomendar(criterios);

        return switch (resultado) {
            case ResultadoRecomendacion.Exitoso exitoso -> RecomendacionResponse.exitosa(exitoso.lugares());
            case ResultadoRecomendacion.SinCoincidencias sinCoincidencias ->
                    RecomendacionResponse.sinCoincidencias(sinCoincidencias.criterioLimitante());
        };
    }
}
