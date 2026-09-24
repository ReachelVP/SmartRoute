package cr.ac.una.smartroute.presentation;

import cr.ac.una.smartroute.business.ConfirmarItinerarioService;
import cr.ac.una.smartroute.data.entity.Itinerario;
import cr.ac.una.smartroute.presentation.dto.ConfirmarItinerarioRequest;
import cr.ac.una.smartroute.presentation.dto.ItinerarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/itinerarios")
public class ItinerarioController {

    private final ConfirmarItinerarioService confirmarItinerarioService;

    public ItinerarioController(ConfirmarItinerarioService confirmarItinerarioService) {
        this.confirmarItinerarioService = confirmarItinerarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItinerarioResponse confirmar(@Valid @RequestBody ConfirmarItinerarioRequest request) {
        Itinerario itinerario = confirmarItinerarioService.confirmar(request);
        return ItinerarioResponse.from(itinerario);
    }
}
