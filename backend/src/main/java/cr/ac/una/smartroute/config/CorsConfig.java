package cr.ac.una.smartroute.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Origen permitido configurable por propiedad (Laboratorio 6): en dev es
 * {@code http://localhost:5173} (Vite); en prod, la URL real de la SPA desplegada, vía la
 * variable de entorno {@code CORS_ALLOWED_ORIGIN} (ver application-prod.properties).
 *
 * <p>Expuesto como {@link CorsConfigurationSource} (no como {@code WebMvcConfigurer}) a propósito:
 * con Spring Security de por medio, el CORS de Spring MVC nunca llega a ejecutarse para una
 * petición OPTIONS de preflight, porque los filtros de seguridad la bloquean primero (exigen
 * autenticación antes de que MVC decida nada de CORS). Con este bean conectado a
 * {@code http.cors(...)} en {@code SecurityConfig}, Spring Security resuelve el preflight con su
 * propio {@code CorsFilter}, antes que cualquier chequeo de autenticación/autorización — así la SPA
 * puede hacer login y llamar a {@code /api/v1} entre orígenes.
 */
@Configuration
public class CorsConfig {

    @Value("${smartroute.cors.allowed-origin:http://localhost:5173}")
    private String allowedOrigin;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(List.of(allowedOrigin));
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // /** y no solo /api/**: el módulo /admin es same-origin (navegación normal, no fetch) así
        // que estas cabeceras no le hacen nada, pero /auth/login sí las necesita y está fuera de
        // /api -- más simple aplicar una sola política a todo que mantener dos registros en sync.
        source.registerCorsConfiguration("/**", configuracion);
        return source;
    }
}
