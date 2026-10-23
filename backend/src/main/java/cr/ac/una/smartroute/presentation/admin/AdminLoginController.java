package cr.ac.una.smartroute.presentation.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Vista de login del módulo administrativo (formLogin con sesión, ver {@code SecurityConfig}). */
@Controller
public class AdminLoginController {

    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }
}
