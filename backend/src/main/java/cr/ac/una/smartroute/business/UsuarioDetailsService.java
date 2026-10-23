package cr.ac.una.smartroute.business;

import cr.ac.una.smartroute.data.entity.Usuario;
import cr.ac.una.smartroute.data.repository.UsuarioRepository;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Puente hacia {@code UsuarioRepository} para el login con sesión (formLogin) del módulo
 * administrativo Thymeleaf (Laboratorio 6) — Spring Security usa esto para validar credenciales y
 * resolver autoridades, con el mismo {@code PasswordEncoder} (BCrypt) que ya usa {@link
 * AuthService} para el login con JWT de la API. Son dos mecanismos de autenticación (sesión vs.
 * token) sobre el mismo usuario y la misma contraseña, no dos sistemas de usuarios distintos.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) {
        Usuario usuario = usuarioRepository
                .findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario con el correo " + correo));

        return new org.springframework.security.core.userdetails.User(
                usuario.getCorreo(),
                usuario.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol())));
    }
}
