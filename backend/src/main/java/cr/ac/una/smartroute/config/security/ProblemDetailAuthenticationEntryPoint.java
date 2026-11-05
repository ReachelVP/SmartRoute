package cr.ac.una.smartroute.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 401, en el mismo formato Problem Details (RFC 9457) que el resto de la API. El cuerpo es fijo
 * (sin datos del request), así que se escribe el JSON a mano en vez de inyectar un ObjectMapper:
 * evita depender de qué variante de Jackson (2 o 3) autoconfigura Spring Boot 4 en este punto tan
 * temprano del arranque del contexto de seguridad.
 */
@Component
public class ProblemDetailAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(ProblemDetailAuthenticationEntryPoint.class);

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        log.warn(
                "evento_seguridad=no_autenticado metodo=\"{}\" ruta=\"{}\"",
                request.getMethod(),
                request.getRequestURI());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter()
                .write(
                        "{\"type\":\"about:blank\",\"title\":\"No autenticado\",\"status\":401,"
                                + "\"detail\":\"Token ausente, inválido o expirado.\"}");
    }
}
