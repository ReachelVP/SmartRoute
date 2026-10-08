package cr.ac.una.smartroute.business;

import cr.ac.una.smartroute.business.exception.ItinerarioInexistenteException;
import cr.ac.una.smartroute.business.exception.RecursoNoPropioException;
import cr.ac.una.smartroute.data.entity.Itinerario;
import cr.ac.una.smartroute.data.repository.ItinerarioRepository;
import cr.ac.una.smartroute.data.specification.ItinerarioSpecifications;
import java.time.LocalDate;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lecturas de Itinerario, separadas de {@link ConfirmarItinerarioService} (que es, a propósito,
 * solo la escritura transaccional). Acá vive la verificación de propiedad del recurso (OWASP
 * API1): un usuario sin rol ADMIN solo puede ver sus propios itinerarios.
 */
@Service
public class ItinerarioConsultaService {

    private final ItinerarioRepository itinerarioRepository;

    public ItinerarioConsultaService(ItinerarioRepository itinerarioRepository) {
        this.itinerarioRepository = itinerarioRepository;
    }

    @Transactional(readOnly = true)
    public Page<Itinerario> listarPaginado(
            Long usuarioAutenticadoId,
            boolean esAdmin,
            Long usuarioIdFiltro,
            String estado,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Pageable pageable) {
        Long usuarioEfectivo = esAdmin ? usuarioIdFiltro : usuarioAutenticadoId;
        Page<Itinerario> pagina = itinerarioRepository.findAll(
                ItinerarioSpecifications.conFiltrosOpcionales(usuarioEfectivo, estado, fechaDesde, fechaHasta), pageable);
        pagina.getContent().forEach(this::inicializarParadas);
        return pagina;
    }

    @Transactional(readOnly = true)
    public Itinerario obtener(Long id, Long usuarioAutenticadoId, boolean esAdmin) {
        Itinerario itinerario =
                itinerarioRepository.findById(id).orElseThrow(() -> new ItinerarioInexistenteException(id));

        if (!esAdmin && !itinerario.getUsuario().getId().equals(usuarioAutenticadoId)) {
            throw new RecursoNoPropioException("El itinerario " + id + " no pertenece al usuario autenticado.");
        }

        inicializarParadas(itinerario);
        return itinerario;
    }

    /**
     * Con {@code spring.jpa.open-in-view=false} la sesión se cierra al salir de este método, y
     * {@code ItinerarioResponse.from()} mapea {@code paradas} (y el {@code lugar} de cada una)
     * fuera de esa transacción, en el controller — hay que inicializarlas acá, dentro de la
     * transacción, en vez de volver la asociación EAGER en la entidad (eso reintroduciría el N+1
     * que {@code ItinerarioRepositoryIT} verifica que no vuelva a aparecer).
     */
    private void inicializarParadas(Itinerario itinerario) {
        Hibernate.initialize(itinerario.getParadas());
        itinerario.getParadas().forEach(parada -> Hibernate.initialize(parada.getLugar()));
    }
}
