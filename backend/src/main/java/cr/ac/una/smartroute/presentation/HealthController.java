package cr.ac.una.smartroute.presentation;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/salud")
    public Map<String, String> salud() {
        return Map.of(
                "status", "UP",
                "service", "SmartRoute");
    }
}
