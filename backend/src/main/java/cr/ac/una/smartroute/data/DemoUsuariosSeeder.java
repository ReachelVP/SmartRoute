package cr.ac.una.smartroute.data;

import cr.ac.una.smartroute.data.entity.Usuario;
import cr.ac.una.smartroute.data.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Los usuarios sembrados en V2__datos_semilla.sql traen un {@code password_hash} de relleno
 * ({@code demo-hash-no-real}) porque Flyway no puede invocar al {@link PasswordEncoder} real de la
 * aplicación. Este seeder completa esa parte al arrancar, igual de idempotente que
 * {@code RecomendacionLogSeeder}: si el hash ya no es el de relleno, no hace nada.
 *
 * <p>Contraseña de demostración para ambos usuarios de ejemplo: {@code Demo1234!}
 */
@Component
public class DemoUsuariosSeeder implements CommandLineRunner {

    private static final String HASH_DE_RELLENO = "demo-hash-no-real";
    private static final String PASSWORD_DEMO = "Demo1234!";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoUsuariosSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        for (Usuario usuario : usuarioRepository.findAll()) {
            if (HASH_DE_RELLENO.equals(usuario.getPasswordHash())) {
                usuario.setPasswordHash(passwordEncoder.encode(PASSWORD_DEMO));
                usuarioRepository.save(usuario);
            }
        }
    }
}
