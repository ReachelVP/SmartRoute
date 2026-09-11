package cr.ac.una.smartroute.data.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cr.ac.una.smartroute.AbstractIntegrationTest;
import cr.ac.una.smartroute.data.entity.Itinerario;
import cr.ac.una.smartroute.data.entity.ItinerarioParada;
import cr.ac.una.smartroute.data.specification.ItinerarioSpecifications;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Evidencia y corrección del problema N+1 (Itinerario → ItinerarioParada), más una de las 4
 * consultas de negocio como Specification. Ver también docs/consultas-negocio.md.
 */
@SpringBootTest
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class ItinerarioRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private ItinerarioRepository itinerarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LugarRepository lugarRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void findAll_generaUnaConsultaPorItinerarioAlAccederParadas_evidenciaDelNMasUno() {
        crearItinerarioConParadas(LocalDate.now().plusDays(1), 2);
        crearItinerarioConParadas(LocalDate.now().plusDays(2), 2);
        entityManager.flush();
        entityManager.clear();

        Statistics stats = estadisticas();
        stats.clear();

        List<Itinerario> todos = itinerarioRepository.findAll();
        todos.forEach(i -> i.getParadas().size()); // dispara el lazy load: una consulta por cada uno

        long consultas = stats.getPrepareStatementCount();
        assertTrue(
                consultas > todos.size(),
                "Se esperaban más consultas que itinerarios (1 por los padres + 1 por cada uno de sus "
                        + "hijos), pero se ejecutaron " + consultas + " para " + todos.size() + " itinerarios");
    }

    @Test
    @Transactional
    void itinerariosConfirmadosDeUsuario_traeParadasEnUnaSolaConsulta_correccionDelNMasUno() {
        crearItinerarioConParadas(LocalDate.now().plusDays(3), 2);
        crearItinerarioConParadas(LocalDate.now().plusDays(4), 2);
        entityManager.flush();
        entityManager.clear();

        Statistics stats = estadisticas();
        stats.clear();

        List<Itinerario> conParadas = itinerarioRepository.itinerariosConfirmadosDeUsuario(1L);
        conParadas.forEach(i -> i.getParadas().size()); // ya vienen cargadas, no dispara nada nuevo

        assertEquals(1, stats.getPrepareStatementCount());
        assertTrue(conParadas.stream().allMatch(i -> !i.getParadas().isEmpty()));
    }

    /** Consulta de negocio (Specification, 2 de 2): "mis itinerarios" con filtros opcionales. */
    @Test
    @Transactional
    void conFiltrosOpcionales_filtraPorUsuarioYEstado() {
        crearItinerarioConParadas(LocalDate.now().plusDays(5), 1);

        Itinerario borrador = new Itinerario();
        borrador.setUsuario(usuarioRepository.getReferenceById(1L));
        borrador.setFecha(LocalDate.now().plusDays(6));
        borrador.setPresupuestoTotalEstimado(new BigDecimal("1000.00"));
        borrador.setEstado("BORRADOR");
        borrador.setCreadoEn(LocalDateTime.now());
        itinerarioRepository.save(borrador);

        List<Itinerario> confirmados = itinerarioRepository.findAll(
                ItinerarioSpecifications.conFiltrosOpcionales(1L, "CONFIRMADO", null, null));

        assertTrue(confirmados.stream().allMatch(i -> i.getEstado().equals("CONFIRMADO")));
        assertTrue(confirmados.stream().allMatch(i -> i.getUsuario().getId().equals(1L)));
        assertTrue(confirmados.stream().noneMatch(i -> i.getId().equals(borrador.getId())));
    }

    private Statistics estadisticas() {
        return entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    }

    private Itinerario crearItinerarioConParadas(LocalDate fecha, int cantidadParadas) {
        Itinerario itinerario = new Itinerario();
        itinerario.setUsuario(usuarioRepository.getReferenceById(1L)); // Reachel
        itinerario.setFecha(fecha);
        itinerario.setPresupuestoTotalEstimado(new BigDecimal("5000.00"));
        itinerario.setEstado("CONFIRMADO");
        itinerario.setCreadoEn(LocalDateTime.now());

        for (int i = 1; i <= cantidadParadas; i++) {
            ItinerarioParada parada = new ItinerarioParada();
            parada.setItinerario(itinerario);
            parada.setLugar(lugarRepository.getReferenceById(3L)); // Museo Nacional
            parada.setOrden((short) i);
            parada.setHoraInicio(LocalTime.of(8 + i, 0));
            parada.setHoraFin(LocalTime.of(9 + i, 0));
            parada.setCostoEstimado(new BigDecimal("1000.00"));
            itinerario.getParadas().add(parada);
        }

        return itinerarioRepository.save(itinerario);
    }
}
