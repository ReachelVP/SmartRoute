package cr.ac.una.smartroute.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Lee el usuario autenticado directamente del {@link SecurityContextHolder}. El principal es el
 * id del usuario (ver {@link JwtAuthenticationFilter}) — nunca se confía en un {@code usuarioId}
 * que venga en el cuerpo de la petición (OWASP API1: Broken Object Level Authorization).
 */
public final class UsuarioAutenticado {

    private UsuarioAutenticado() {
    }

    public static Long id() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()) {
            throw new IllegalStateException("No hay un usuario autenticado en el contexto de seguridad.");
        }
        return Long.valueOf((String) autenticacion.getPrincipal());
    }

    public static boolean esAdmin() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        return autenticacion != null
                && autenticacion.getAuthorities().stream()
                        .anyMatch(autoridad -> autoridad.getAuthority().equals("ROLE_ADMIN"));
    }
}
