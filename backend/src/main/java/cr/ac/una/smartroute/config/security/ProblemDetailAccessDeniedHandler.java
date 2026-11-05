package cr.ac.una.smartroute.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * 403, en el mismo formato Problem Details (RFC 9457) que el resto de la API. El cuerpo es fijo
 * (sin datos del request), así que se escribe el JSON a mano en vez de inyectar un ObjectMapper:
 * evita depender de qué variante de Jackson (2 o 3) autoconfigura Spring Boot 4 en este punto tan
 * temprano del arranque del contexto de seguridad.
 */
@Component
public class ProblemDetailAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(ProblemDetailAccessDeniedHandler.class);

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        log.warn(
                "evento_seguridad=acceso_denegado metodo=\"{}\" ruta=\"{}\" motivo=\"{}\"",
                request.getMethod(),
                request.getRequestURI(),
                accessDeniedException.getMessage());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter()
                .write(
                        "{\"type\":\"about:blank\",\"title\":\"Acceso denegado\",\"status\":403,"
                                + "\"detail\":\"No tenés permiso para realizar esta operación.\"}");
    }
}
