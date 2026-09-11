package cr.ac.una.smartroute.data.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cr.ac.una.smartroute.AbstractIntegrationTest;
import cr.ac.una.smartroute.data.entity.Lugar;
import cr.ac.una.smartroute.data.specification.LugarSpecifications;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** Pruebas de integración contra SQL Server real (Testcontainers). */
@SpringBootTest
class LugarRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private LugarRepository lugarRepository;

    @Autowired
    private ProvinciaRepository provinciaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    @Transactional
    void guardarYEncontrarLugar_roundTripBasico() {
        Lugar nuevo = new Lugar();
        nuevo.setNombre("Jardín Botánico Lankester");
        nuevo.setProvincia(provinciaRepository.getReferenceById(1L)); // San José
        nuevo.setCategoria(categoriaRepository.getReferenceById(1L)); // Natural
        nuevo.setCostoEstimado(new BigDecimal("6000.00"));
        nuevo.setNivelAccesibilidad("MEDIA");
        nuevo.setRequiereTransportePropio(true);
        nuevo.setActivo(true);
        nuevo.setCreadoEn(LocalDateTime.now());

        Lugar guardado = lugarRepository.save(nuevo);

        Optional<Lugar> encontrado = lugarRepository.findById(guardado.getId());
        assertTrue(encontrado.isPresent());
        assertEquals("Jardín Botánico Lankester", encontrado.get().getNombre());
        assertEquals("San José", encontrado.get().getProvincia().getNombre());
    }

    /**
     * Consulta de negocio (Specification, 1 de 2). Usa el catálogo sembrado por
     * V2__datos_semilla.sql: en San José hay Museo Nacional (3000), Teatro Nacional (2000) y
     * Mercado Central (5000); con presupuesto máximo 4000 solo los dos primeros califican.
     */
    @Test
    void conFiltrosOpcionales_combinaProvinciaYPresupuesto() {
        List<Lugar> resultado = lugarRepository.findAll(
                LugarSpecifications.conFiltrosOpcionales(1L, null, new BigDecimal("4000.00"), null));

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(l -> l.getProvincia().getId().equals(1L)));
        assertTrue(resultado.stream()
                .allMatch(l -> l.getCostoEstimado().compareTo(new BigDecimal("4000.00")) <= 0));
    }
}
