package cr.ac.una.smartroute.business;

import cr.ac.una.smartroute.business.exception.CredencialesInvalidasException;
import cr.ac.una.smartroute.config.security.JwtService;
import cr.ac.una.smartroute.data.entity.Usuario;
import cr.ac.una.smartroute.data.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResultado login(String correo, String password) {
        Usuario usuario = usuarioRepository
                .findByCorreo(correo)
                .filter(u -> passwordEncoder.matches(password, u.getPasswordHash()))
                .orElseThrow(CredencialesInvalidasException::new);

        String token = jwtService.generarToken(usuario.getId(), usuario.getRol());
        return new LoginResultado(token, usuario.getId(), usuario.getNombre(), usuario.getRol());
    }
}
