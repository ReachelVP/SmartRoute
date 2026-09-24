package cr.ac.una.smartroute.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cr.ac.una.smartroute.business.exception.HorariosSolapadosException;
import cr.ac.una.smartroute.business.exception.LugarInexistenteOInactivoException;
import cr.ac.una.smartroute.business.exception.PerfilPreferenciasIncompletoException;
import cr.ac.una.smartroute.business.exception.PresupuestoExcedidoException;
import cr.ac.una.smartroute.business.exception.TransportePropioRequeridoException;
import cr.ac.una.smartroute.data.entity.Categoria;
import cr.ac.una.smartroute.data.entity.Itinerario;
import cr.ac.una.smartroute.data.entity.Lugar;
import cr.ac.una.smartroute.data.entity.PerfilPreferencias;
import cr.ac.una.smartroute.data.entity.Provincia;
import cr.ac.una.smartroute.data.entity.Usuario;
import cr.ac.una.smartroute.data.repository.ItinerarioRepository;
import cr.ac.una.smartroute.data.repository.LugarRepository;
import cr.ac.una.smartroute.data.repository.PerfilPreferenciasRepository;
import cr.ac.una.smartroute.presentation.dto.ConfirmarItinerarioRequest;
import cr.ac.una.smartroute.presentation.dto.ParadaConfirmacionRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas unitarias del Proceso 2: reglas del servicio, sin Spring ni base de datos real. */
@ExtendWith(MockitoExtension.class)
class ConfirmarItinerarioServiceTest {

    @Mock
    private PerfilPreferenciasRepository perfilPreferenciasRepository;

    @Mock
    private LugarRepository lugarRepository;

    @Mock
    private ItinerarioRepository itinerarioRepository;

    private ConfirmarItinerarioService service;

    private final Provincia sanJose = provincia(1L, "San José");
    private final Categoria historica = categoria(1L, "Histórica");
    private final Usuario reachel = usuario(1L, sanJose);
    private final Lugar museo = lugar(3L, "Museo Nacional", historica, sanJose, "3000.00", false, true);
    private final Lugar teatro = lugar(4L, "Teatro Nacional", historica, sanJose, "2000.00", false, true);

    @BeforeEach
    void setUp() {
        service = new ConfirmarItinerarioService(perfilPreferenciasRepository, lugarRepository, itinerarioRepository);
    }

    @Test
    void confirmar_lanzaPerfilIncompleto_siElUsuarioNoTienePerfil() {
        when(perfilPreferenciasRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());
        ConfirmarItinerarioRequest request = requestCon(false, parada(museo, "09:00", "11:00"));

        assertThrows(PerfilPreferenciasIncompletoException.class, () -> service.confirmar(request));
        verify(itinerarioRepository, never()).save(any());
    }

    @Test
    void confirmar_lanzaLugarInvalido_siElLugarNoExiste() {
        when(perfilPreferenciasRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(perfil(reachel, historica, "10000.00", false)));
        when(lugarRepository.findById(99L)).thenReturn(Optional.empty());
        ConfirmarItinerarioRequest request =
                requestCon(false, new ParadaConfirmacionRequest(99L, LocalTime.of(9, 0), LocalTime.of(11, 0)));

        assertThrows(LugarInexistenteOInactivoException.class, () -> service.confirmar(request));
        verify(itinerarioRepository, never()).save(any());
    }

    @Test
    void confirmar_lanzaLugarInvalido_siElLugarEstaInactivo() {
        Lugar inactivo = lugar(5L, "Cerrado", historica, sanJose, "1000.00", false, true);
        inactivo.setActivo(false);
        when(perfilPreferenciasRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(perfil(reachel, historica, "10000.00", false)));
        when(lugarRepository.findById(5L)).thenReturn(Optional.of(inactivo));
        ConfirmarItinerarioRequest request = requestCon(false, parada(inactivo, "09:00", "11:00"));

        assertThrows(LugarInexistenteOInactivoException.class, () -> service.confirmar(request));
    }

    @Test
    void confirmar_lanzaTransporteRequerido_siUsuarioNoTieneTransporteYElLugarLoExige() {
        Lugar requiereTransporte = lugar(6L, "Volcán Poás", historica, sanJose, "8000.00", true, true);
        when(perfilPreferenciasRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(perfil(reachel, historica, "10000.00", false)));
        when(lugarRepository.findById(6L)).thenReturn(Optional.of(requiereTransporte));
        ConfirmarItinerarioRequest request = requestCon(false, parada(requiereTransporte, "09:00", "11:00"));

        assertThrows(TransportePropioRequeridoException.class, () -> service.confirmar(request));
        verify(itinerarioRepository, never()).save(any());
    }

    @Test
    void confirmar_lanzaHorariosSolapados_siNoHayMargenMinimoEntreParadas() {
        when(perfilPreferenciasRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(perfil(reachel, historica, "10000.00", false)));
        when(lugarRepository.findById(3L)).thenReturn(Optional.of(museo));
        when(lugarRepository.findById(4L)).thenReturn(Optional.of(teatro));
        // El museo termina a las 11:00 y el teatro arranca a las 11:05: no alcanza el margen de 15 min.
        ConfirmarItinerarioRequest request =
                requestCon(false, parada(museo, "09:00", "11:00"), parada(teatro, "11:05", "12:00"));

        assertThrows(HorariosSolapadosException.class, () -> service.confirmar(request));
        verify(itinerarioRepository, never()).save(any());
    }

    @Test
    void confirmar_lanzaHorariosSolapados_siHoraFinNoEsPosteriorAHoraInicio() {
        when(perfilPreferenciasRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(perfil(reachel, historica, "10000.00", false)));
        when(lugarRepository.findById(3L)).thenReturn(Optional.of(museo));
        ConfirmarItinerarioRequest request = requestCon(false, parada(museo, "11:00", "10:00"));

        assertThrows(HorariosSolapadosException.class, () -> service.confirmar(request));
    }

    @Test
    void confirmar_guardaYLuegoLanzaPresupuestoExcedido_siSuperaElMaximoSinConfirmarlo() {
        when(perfilPreferenciasRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(perfil(reachel, historica, "1000.00", false))); // museo cuesta 3000
        when(lugarRepository.findById(3L)).thenReturn(Optional.of(museo));
        when(itinerarioRepository.save(any(Itinerario.class))).thenAnswer(inv -> inv.getArgument(0));
        ConfirmarItinerarioRequest request = requestCon(false, parada(museo, "09:00", "11:00"));

        assertThrows(PresupuestoExcedidoException.class, () -> service.confirmar(request));

        // La escritura sí ocurrió antes de la excepción: es @Transactional quien deshace todo,
        // no una validación previa que evitó guardar.
        verify(itinerarioRepository, times(1)).save(any(Itinerario.class));
    }

    @Test
    void confirmar_noLanzaExcepcion_siSuperaPresupuestoPeroElUsuarioLoConfirmoExplicitamente() {
        when(perfilPreferenciasRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(perfil(reachel, historica, "1000.00", false)));
        when(lugarRepository.findById(3L)).thenReturn(Optional.of(museo));
        when(itinerarioRepository.save(any(Itinerario.class))).thenAnswer(inv -> inv.getArgument(0));
        ConfirmarItinerarioRequest request = requestCon(true, parada(museo, "09:00", "11:00"));

        Itinerario resultado = service.confirmar(request);

        assertEquals(new BigDecimal("3000.00"), resultado.getPresupuestoTotalEstimado());
    }

    @Test
    void confirmar_devuelveItinerarioConfirmado_caminoFeliz() {
        when(perfilPreferenciasRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(perfil(reachel, historica, "10000.00", false)));
        when(lugarRepository.findById(3L)).thenReturn(Optional.of(museo));
        when(lugarRepository.findById(4L)).thenReturn(Optional.of(teatro));
        when(itinerarioRepository.save(any(Itinerario.class))).thenAnswer(inv -> inv.getArgument(0));
        ConfirmarItinerarioRequest request =
                requestCon(false, parada(museo, "09:00", "11:00"), parada(teatro, "11:30", "13:00"));

        Itinerario resultado = service.confirmar(request);

        assertEquals("CONFIRMADO", resultado.getEstado());
        assertEquals(new BigDecimal("5000.00"), resultado.getPresupuestoTotalEstimado());
        assertEquals(2, resultado.getParadas().size());
        assertTrue(resultado.getParadas().stream().anyMatch(p -> p.getOrden() == 1));
        assertTrue(resultado.getParadas().stream().anyMatch(p -> p.getOrden() == 2));
    }

    private ConfirmarItinerarioRequest requestCon(
            boolean confirmarExcesoPresupuesto, ParadaConfirmacionRequest... paradas) {
        return new ConfirmarItinerarioRequest(
                1L, LocalDate.now().plusDays(1), List.of(paradas), confirmarExcesoPresupuesto);
    }

    private static ParadaConfirmacionRequest parada(Lugar lugar, String horaInicio, String horaFin) {
        return new ParadaConfirmacionRequest(lugar.getId(), LocalTime.parse(horaInicio), LocalTime.parse(horaFin));
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

    private static Usuario usuario(Long id, Provincia provincia) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Reachel");
        usuario.setCorreo("reachel@ejemplo.com");
        usuario.setPasswordHash("hash");
        usuario.setProvincia(provincia);
        return usuario;
    }

    private static Lugar lugar(
            Long id,
            String nombre,
            Categoria categoria,
            Provincia provincia,
            String costo,
            boolean requiereTransporte,
            boolean activo) {
        Lugar lugar = new Lugar();
        lugar.setId(id);
        lugar.setNombre(nombre);
        lugar.setCategoria(categoria);
        lugar.setProvincia(provincia);
        lugar.setCostoEstimado(new BigDecimal(costo));
        lugar.setNivelAccesibilidad("ALTA");
        lugar.setRequiereTransportePropio(requiereTransporte);
        lugar.setActivo(activo);
        return lugar;
    }

    private static PerfilPreferencias perfil(
            Usuario usuario, Categoria categoriaInteres, String presupuestoMaximo, boolean tieneTransportePropio) {
        PerfilPreferencias perfil = new PerfilPreferencias();
        perfil.setUsuario(usuario);
        perfil.setPresupuestoMaximo(new BigDecimal(presupuestoMaximo));
        perfil.setTieneTransportePropio(tieneTransportePropio);
        perfil.getCategoriasInteres().add(categoriaInteres);
        return perfil;
    }
}
