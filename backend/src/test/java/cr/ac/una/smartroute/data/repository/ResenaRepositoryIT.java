package cr.ac.una.smartroute.data.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cr.ac.una.smartroute.AbstractIntegrationTest;
import cr.ac.una.smartroute.data.entity.Resena;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class ResenaRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LugarRepository lugarRepository;

    /**
     * Consulta de negocio (JPQL, 1 de 2). El Museo Nacional (lugar id 3) ya tiene una reseña
     * sembrada de Reachel (usuario 1) con calificación 5; se agrega una segunda de Kimberley
     * (usuario 2) con calificación 3, para probar la agregación real, no solo el paso de un único
     * valor.
     */
    @Test
    @Transactional
    void promedioCalificacionPorLugar_promediaVariasResenas() {
        Resena segundaResena = new Resena();
        segundaResena.setUsuario(usuarioRepository.getReferenceById(2L));
        segundaResena.setLugar(lugarRepository.getReferenceById(3L));
        segundaResena.setCalificacion((short) 3);
        segundaResena.setComentario("Bien, pero muy concurrido.");
        segundaResena.setCreadoEn(LocalDateTime.now());
        resenaRepository.save(segundaResena);

        Optional<Double> promedio = resenaRepository.promedioCalificacionPorLugar(3L);

        assertEquals(4.0, promedio.orElseThrow()); // (5 + 3) / 2
    }
}
