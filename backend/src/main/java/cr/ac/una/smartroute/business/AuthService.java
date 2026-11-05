package cr.ac.una.smartroute.business;

import cr.ac.una.smartroute.business.exception.CredencialesInvalidasException;
import cr.ac.una.smartroute.config.security.JwtService;
import cr.ac.una.smartroute.config.security.LimitadorIntentosLogin;
import cr.ac.una.smartroute.data.entity.Usuario;
import cr.ac.una.smartroute.data.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LimitadorIntentosLogin limitadorIntentosLogin;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            LimitadorIntentosLogin limitadorIntentosLogin) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.limitadorIntentosLogin = limitadorIntentosLogin;
    }

    @Transactional(readOnly = true)
    public LoginResultado login(String correo, String password) {
        // Laboratorio 7: antes de siquiera consultar la base, rechaza si este correo ya superó el
        // límite de intentos fallidos recientes (fuerza bruta -- OWASP Top 10:2025 A06/A07).
        limitadorIntentosLogin.verificarNoBloqueado(correo);

        Usuario usuario = usuarioRepository
                .findByCorreo(correo)
                .filter(u -> passwordEncoder.matches(password, u.getPasswordHash()))
                .orElseThrow(() -> {
                    // Nunca se loguea la contraseña -- solo el correo con el que se intentó, para
                    // poder detectar fuerza bruta contra una cuenta puntual.
                    log.warn("evento_seguridad=login_fallido correo=\"{}\"", correo);
                    limitadorIntentosLogin.registrarFallo(correo);
                    return new CredencialesInvalidasException();
                });

        limitadorIntentosLogin.registrarExito(correo);
        String token = jwtService.generarToken(usuario.getId(), usuario.getRol());
        return new LoginResultado(token, usuario.getId(), usuario.getNombre(), usuario.getRol());
    }
}
