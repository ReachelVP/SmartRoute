package cr.ac.una.smartroute.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cr.ac.una.smartroute.data.entity.Categoria;
import cr.ac.una.smartroute.data.entity.Lugar;
import cr.ac.una.smartroute.data.entity.Provincia;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Los filtros de {@link RecomendacionFilters} son funciones puras: estas pruebas no levantan
 * Spring ni tocan una base de datos, solo construyen objetos en memoria.
 */
class RecomendacionFiltersTest {

    private static final Provincia SAN_JOSE = provincia(1L, "San José");
    private static final Provincia CARTAGO = provincia(2L, "Cartago");

    private static final Categoria HISTORICA = categoria(1L, "Histórica");
    private static final Categoria CULTURAL = categoria(2L, "Cultural");
    private static final Categoria NATURAL = categoria(3L, "Natural");

    private static final Lugar MUSEO =
            lugar(1L, "Museo Nacional", HISTORICA, SAN_JOSE, "3000.00", false);
    private static final Lugar TEATRO =
            lugar(2L, "Teatro Nacional", CULTURAL, SAN_JOSE, "2000.00", false);
    private static final Lugar VOLCAN =
            lugar(3L, "Volcán Poás", NATURAL, CARTAGO, "8000.00", true);

    @Test
    void porCategoriaDeInteres_mantieneSoloLosDeLasCategoriasIndicadas() {
        List<Lugar> resultado = RecomendacionFilters.porCategoriaDeInteres(
                List.of(MUSEO, TEATRO, VOLCAN), Set.of(NATURAL.getId()));

        assertEquals(List.of(VOLCAN), resultado);
    }

    @Test
    void porProvincia_mantieneSoloLosDeLaProvinciaIndicada() {
        List<Lugar> resultado =
                RecomendacionFilters.porProvincia(List.of(MUSEO, TEATRO, VOLCAN), SAN_JOSE.getId());

        assertEquals(List.of(MUSEO, TEATRO), resultado);
    }

    @Test
    void excluyendoTransporteRequeridoSiNoTiene_excluyeLosQueLoRequierenCuandoNoTieneTransporte() {
        List<Lugar> resultado = RecomendacionFilters.excluyendoTransporteRequeridoSiNoTiene(
                List.of(MUSEO, VOLCAN), false);

        assertEquals(List.of(MUSEO), resultado);
    }

    @Test
    void excluyendoTransporteRequeridoSiNoTiene_noExcluyeNadaCuandoSiTieneTransporte() {
        List<Lugar> resultado = RecomendacionFilters.excluyendoTransporteRequeridoSiNoTiene(
                List.of(MUSEO, VOLCAN), true);

        assertEquals(List.of(MUSEO, VOLCAN), resultado);
    }

    @Test
    void dentroDePresupuesto_excluyeLosMasCarosQueElPresupuesto() {
        List<Lugar> resultado = RecomendacionFilters.dentroDePresupuesto(
                List.of(MUSEO, VOLCAN), new BigDecimal("5000.00"));

        assertEquals(List.of(MUSEO), resultado);
    }

    @Test
    void aplicar_devuelveExitosoConLosLugaresQueCumplenLosCuatroFiltros() {
        CriteriosRecomendacion criterios = new CriteriosRecomendacion(
                SAN_JOSE.getId(),
                Set.of(HISTORICA.getId(), CULTURAL.getId()),
                new BigDecimal("5000.00"),
                false);

        ResultadoRecomendacion resultado =
                RecomendacionFilters.aplicar(List.of(MUSEO, TEATRO, VOLCAN), criterios);

        var exitoso = assertInstanceOf(ResultadoRecomendacion.Exitoso.class, resultado);
        assertEquals(List.of(MUSEO, TEATRO), exitoso.lugares());
    }

    @Test
    void aplicar_reportaCategoriasDeInteres_cuandoNingunLugarEsDeEsasCategorias() {
        CriteriosRecomendacion criterios =
                new CriteriosRecomendacion(SAN_JOSE.getId(), Set.of(NATURAL.getId()), new BigDecimal("99999"), true);

        ResultadoRecomendacion resultado = RecomendacionFilters.aplicar(List.of(MUSEO, TEATRO), criterios);

        assertCriterioLimitante(resultado, "categorías de interés");
    }

    @Test
    void aplicar_reportaProvincia_cuandoNingunLugarEsDeLaProvinciaElegida() {
        CriteriosRecomendacion criterios = new CriteriosRecomendacion(
                CARTAGO.getId(), Set.of(HISTORICA.getId(), CULTURAL.getId()), new BigDecimal("99999"), true);

        ResultadoRecomendacion resultado = RecomendacionFilters.aplicar(List.of(MUSEO, TEATRO), criterios);

        assertCriterioLimitante(resultado, "provincia");
    }

    @Test
    void aplicar_reportaTransportePropio_cuandoTodosLosSobrevivientesLoRequierenYNoTiene() {
        CriteriosRecomendacion criterios =
                new CriteriosRecomendacion(CARTAGO.getId(), Set.of(NATURAL.getId()), new BigDecimal("99999"), false);

        ResultadoRecomendacion resultado = RecomendacionFilters.aplicar(List.of(VOLCAN), criterios);

        assertCriterioLimitante(resultado, "transporte propio");
    }

    @Test
    void aplicar_reportaPresupuesto_cuandoTodosLosSobrevivientesLoSuperan() {
        CriteriosRecomendacion criterios = new CriteriosRecomendacion(
                SAN_JOSE.getId(), Set.of(HISTORICA.getId(), CULTURAL.getId()), new BigDecimal("500.00"), false);

        ResultadoRecomendacion resultado = RecomendacionFilters.aplicar(List.of(MUSEO, TEATRO), criterios);

        assertCriterioLimitante(resultado, "presupuesto");
    }

    private static void assertCriterioLimitante(ResultadoRecomendacion resultado, String criterioEsperado) {
        var sinCoincidencias = assertInstanceOf(ResultadoRecomendacion.SinCoincidencias.class, resultado);
        assertEquals(criterioEsperado, sinCoincidencias.criterioLimitante());
        assertTrue(sinCoincidencias.criterioLimitante() != null && !sinCoincidencias.criterioLimitante().isBlank());
    }

    private static Provincia provincia(Long id, String nombre) {
        Provincia provincia = new Provincia();
        provincia.setId(id);
        provincia.setNombre(nombre);
        return provincia;
    }

    private static Categoria categoria(Long id, String nombre) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre(nombre);
        return categoria;
    }

    private static Lugar lugar(
            Long id,
            String nombre,
            Categoria categoria,
            Provincia provincia,
            String costoEstimado,
            boolean requiereTransportePropio) {
        Lugar lugar = new Lugar();
        lugar.setId(id);
        lugar.setNombre(nombre);
        lugar.setCategoria(categoria);
        lugar.setProvincia(provincia);
        lugar.setCostoEstimado(new BigDecimal(costoEstimado));
        lugar.setNivelAccesibilidad("ALTA");
        lugar.setRequiereTransportePropio(requiereTransportePropio);
        lugar.setActivo(true);
        return lugar;
    }
}
