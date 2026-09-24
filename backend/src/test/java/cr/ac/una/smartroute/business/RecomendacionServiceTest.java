package cr.ac.una.smartroute.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import cr.ac.una.smartroute.business.exception.PerfilPreferenciasIncompletoException;
import cr.ac.una.smartroute.data.entity.Categoria;
import cr.ac.una.smartroute.data.entity.Lugar;
import cr.ac.una.smartroute.data.entity.PerfilPreferencias;
import cr.ac.una.smartroute.data.entity.Provincia;
import cr.ac.una.smartroute.data.repository.LugarRepository;
import cr.ac.una.smartroute.data.repository.PerfilPreferenciasRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas unitarias del Proceso 1: reglas ejercitadas en milisegundos, sin Spring ni base de datos. */
@ExtendWith(MockitoExtension.class)
class RecomendacionServiceTest {

    @Mock
    private LugarRepository lugarRepository;

    @Mock
    private PerfilPreferenciasRepository perfilPreferenciasRepository;

    private RecomendacionService service;

    private final Provincia sanJose = provincia(1L, "San José");
    private final Categoria historica = categoria(1L, "Histórica");
    private final Lugar museo = lugar(3L, "Museo Nacional", historica, sanJose, "3000.00", false);

    @BeforeEach
    void setUp() {
        service = new RecomendacionService(lugarRepository, perfilPreferenciasRepository);
    }

    @Test
    void recomendar_lanzaPerfilIncompleto_siElUsuarioNoTienePerfil() {
        when(perfilPreferenciasRepository.findByUsuarioId(99L)).thenReturn(Optional.empty());

        assertThrows(
                PerfilPreferenciasIncompletoException.class, () -> service.recomendar(99L, sanJose.getId()));
    }

    @Test
    void recomendar_devuelveExitoso_conLugaresQueCumplenElPerfil() {
        PerfilPreferencias perfil = perfil(1L, historica, "5000.00", false);
        when(perfilPreferenciasRepository.findByUsuarioId(1L)).thenReturn(Optional.of(perfil));
        when(lugarRepository.findAll()).thenReturn(List.of(museo));

        ResultadoRecomendacion resultado = service.recomendar(1L, sanJose.getId());

        var exitoso = assertInstanceOf(ResultadoRecomendacion.Exitoso.class, resultado);
        assertEquals(List.of(museo), exitoso.lugares());
    }

    @Test
    void recomendar_devuelveSinCoincidencias_siElPresupuestoDelPerfilEsMuyBajo() {
        PerfilPreferencias perfil = perfil(1L, historica, "10.00", false);
        when(perfilPreferenciasRepository.findByUsuarioId(1L)).thenReturn(Optional.of(perfil));
        when(lugarRepository.findAll()).thenReturn(List.of(museo));

        ResultadoRecomendacion resultado = service.recomendar(1L, sanJose.getId());

        var sinCoincidencias = assertInstanceOf(ResultadoRecomendacion.SinCoincidencias.class, resultado);
        assertEquals("presupuesto", sinCoincidencias.criterioLimitante());
    }

    @Test
    void recomendar_excluyeLugaresInactivos_delCatalogo() {
        Lugar inactivo = lugar(4L, "Cerrado por reforma", historica, sanJose, "1000.00", false);
        inactivo.setActivo(false);
        PerfilPreferencias perfil = perfil(1L, historica, "5000.00", false);
        when(perfilPreferenciasRepository.findByUsuarioId(1L)).thenReturn(Optional.of(perfil));
        when(lugarRepository.findAll()).thenReturn(List.of(museo, inactivo));

        ResultadoRecomendacion resultado = service.recomendar(1L, sanJose.getId());

        var exitoso = assertInstanceOf(ResultadoRecomendacion.Exitoso.class, resultado);
        assertEquals(List.of(museo), exitoso.lugares());
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
            Long id, String nombre, Categoria categoria, Provincia provincia, String costo, boolean requiereTransporte) {
        Lugar lugar = new Lugar();
        lugar.setId(id);
        lugar.setNombre(nombre);
        lugar.setCategoria(categoria);
        lugar.setProvincia(provincia);
        lugar.setCostoEstimado(new BigDecimal(costo));
        lugar.setNivelAccesibilidad("ALTA");
        lugar.setRequiereTransportePropio(requiereTransporte);
        lugar.setActivo(true);
        return lugar;
    }

    private static PerfilPreferencias perfil(
            Long usuarioId, Categoria categoriaInteres, String presupuestoMaximo, boolean tieneTransportePropio) {
        PerfilPreferencias perfil = new PerfilPreferencias();
        perfil.setPresupuestoMaximo(new BigDecimal(presupuestoMaximo));
        perfil.setTieneTransportePropio(tieneTransportePropio);
        perfil.getCategoriasInteres().add(categoriaInteres);
        return perfil;
    }
}
