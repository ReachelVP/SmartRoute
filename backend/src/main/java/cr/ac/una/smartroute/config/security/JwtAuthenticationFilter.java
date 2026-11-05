package cr.ac.una.smartroute.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee el header {@code Authorization: Bearer <token>}, valida el JWT y deja en el
 * {@link SecurityContextHolder} una autenticación cuyo principal es el id del usuario (como
 * String) y cuya autoridad es {@code ROLE_<rol del token>}. Si el token falta o es inválido, no
 * lanza nada aquí — simplemente no autentica, y es la configuración de seguridad la que decide si
 * la ruta pedida requiere autenticación (devolviendo 401 en ese caso).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.parsear(header.substring(7)).getPayload();
                String usuarioId = claims.getSubject();
                String rol = claims.get("rol", String.class);

                var autenticacion = new UsernamePasswordAuthenticationToken(
                        usuarioId, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (JwtException | IllegalArgumentException excepcion) {
                // Nunca se loguea el token ni el header completo -- solo que hubo uno inválido y
                // por qué (expirado, firma inválida, malformado), para diagnosticar sin exponer
                // credenciales en el log.
                log.warn(
                        "evento_seguridad=token_invalido ruta=\"{}\" motivo=\"{}\"",
                        request.getRequestURI(),
                        excepcion.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
