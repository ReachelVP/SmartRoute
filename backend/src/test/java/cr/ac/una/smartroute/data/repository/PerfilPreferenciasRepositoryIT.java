package cr.ac.una.smartroute.data.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import cr.ac.una.smartroute.AbstractIntegrationTest;
import cr.ac.una.smartroute.data.entity.PerfilPreferencias;
import cr.ac.una.smartroute.data.entity.Usuario;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Valida en la base real la decisión {@code ON DELETE CASCADE} de
 * {@code perfil_preferencias.usuario_id} documentada en el Lab 2 (docs/modelo-datos.md): borrar el
 * usuario dueño de un perfil debe borrar ese perfil también. Se usa un usuario nuevo, sin
 * itinerarios ni reseñas (esas FK no tienen cascada a propósito), para aislar justo esa relación.
 */
@SpringBootTest
class PerfilPreferenciasRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilPreferenciasRepository perfilPreferenciasRepository;

    @Autowired
    private ProvinciaRepository provinciaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void eliminarUsuario_eliminaEnCascadaSuPerfilPreferencias() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Prueba Cascada");
        usuario.setCorreo("cascada.test@ejemplo.com");
        usuario.setPasswordHash("hash-de-prueba");
        usuario.setProvincia(provinciaRepository.getReferenceById(1L));
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario = usuarioRepository.save(usuario);

        PerfilPreferencias perfil = new PerfilPreferencias();
        perfil.setUsuario(usuario);
        perfil.setPresupuestoMaximo(new BigDecimal("10000.00"));
        perfil.setTieneTransportePropio(false);
        perfil.setActualizadoEn(LocalDateTime.now());
        perfil = perfilPreferenciasRepository.save(perfil);
        Long perfilId = perfil.getId();

        usuarioRepository.delete(usuario);
        entityManager.flush();
        entityManager.clear(); // sin esto, findById podría devolver la entidad cacheada del 1er nivel

        assertTrue(perfilPreferenciasRepository.findById(perfilId).isEmpty());
    }
}
