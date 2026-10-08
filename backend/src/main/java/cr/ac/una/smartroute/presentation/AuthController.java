package cr.ac.una.smartroute.presentation;

import cr.ac.una.smartroute.business.AuthService;
import cr.ac.una.smartroute.presentation.dto.LoginRequest;
import cr.ac.una.smartroute.presentation.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Único endpoint de autenticación, sin versionar a propósito (así lo pide la guía del Lab 5). */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return LoginResponse.from(authService.login(request.correo(), request.password()));
    }
}
