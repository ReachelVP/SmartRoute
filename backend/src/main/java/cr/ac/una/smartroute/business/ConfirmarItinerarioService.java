package cr.ac.una.smartroute.business;

import cr.ac.una.smartroute.business.exception.HorariosSolapadosException;
import cr.ac.una.smartroute.business.exception.LugarInexistenteOInactivoException;
import cr.ac.una.smartroute.business.exception.PerfilPreferenciasIncompletoException;
import cr.ac.una.smartroute.business.exception.PresupuestoExcedidoException;
import cr.ac.una.smartroute.business.exception.TransportePropioRequeridoException;
import cr.ac.una.smartroute.data.entity.Itinerario;
import cr.ac.una.smartroute.data.entity.Lugar;
import cr.ac.una.smartroute.data.entity.PerfilPreferencias;
import cr.ac.una.smartroute.data.repository.ItinerarioRepository;
import cr.ac.una.smartroute.data.repository.LugarRepository;
import cr.ac.una.smartroute.data.repository.PerfilPreferenciasRepository;
import cr.ac.una.smartroute.presentation.dto.ConfirmarItinerarioRequest;
import cr.ac.una.smartroute.presentation.dto.ParadaConfirmacionRequest;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso completo del Proceso 2 del dominio: confirmar un itinerario es una operación
 * "todo o nada" — el {@code Itinerario} y todas sus {@code ItinerarioParada} se crean en una sola
 * transacción. Si el costo total supera el presupuesto del perfil (sin confirmación explícita), la
 * excepción se lanza después de guardar: {@code @Transactional} deshace también esa escritura, no
 * solo evita una nueva. Es justo ese camino el que prueba
 * {@code ConfirmarItinerarioServiceRollbackIT} con Testcontainers.
 */
@Service
public class ConfirmarItinerarioService {

    /** Margen mínimo de traslado entre el fin de una parada y el inicio de la siguiente. */
    static final Duration MARGEN_MINIMO_TRASLADO = Duration.ofMinutes(15);

    private final PerfilPreferenciasRepository perfilPreferenciasRepository;
    private final LugarRepository lugarRepository;
    private final ItinerarioRepository itinerarioRepository;

    public ConfirmarItinerarioService(
            PerfilPreferenciasRepository perfilPreferenciasRepository,
            LugarRepository lugarRepository,
            ItinerarioRepository itinerarioRepository) {
        this.perfilPreferenciasRepository = perfilPreferenciasRepository;
        this.lugarRepository = lugarRepository;
        this.itinerarioRepository = itinerarioRepository;
    }

    @Transactional
    public Itinerario confirmar(ConfirmarItinerarioRequest request) {
        PerfilPreferencias perfil = perfilPreferenciasRepository
                .findByUsuarioId(request.usuarioId())
                .orElseThrow(() -> new PerfilPreferenciasIncompletoException(request.usuarioId()));

        List<ParadaPlanificada> paradasPlanificadas = resolverYValidarParadas(request.paradas(), perfil);

        Itinerario itinerario =
                ItinerarioFactory.crear(perfil.getUsuario(), request.fecha(), paradasPlanificadas);

        // Las escrituras ocurren aquí (INSERT itinerario + INSERT de cada parada, en cascada) —
        // todavía dentro de la transacción, sin confirmar.
        itinerario = itinerarioRepository.save(itinerario);

        if (!request.confirmarExcesoPresupuesto()
                && itinerario.getPresupuestoTotalEstimado().compareTo(perfil.getPresupuestoMaximo()) > 0) {
            throw new PresupuestoExcedidoException(
                    itinerario.getPresupuestoTotalEstimado(), perfil.getPresupuestoMaximo());
        }

        return itinerario;
    }

    private List<ParadaPlanificada> resolverYValidarParadas(
            List<ParadaConfirmacionRequest> paradasSolicitadas, PerfilPreferencias perfil) {
        List<ParadaPlanificada> planificadas = new ArrayList<>();
        LocalTime finAnterior = null;
        int ordenAnterior = 0;

        for (ParadaConfirmacionRequest solicitada : paradasSolicitadas) {
            Lugar lugar = lugarRepository
                    .findById(solicitada.lugarId())
                    .filter(Lugar::isActivo)
                    .orElseThrow(() -> new LugarInexistenteOInactivoException(solicitada.lugarId()));

            if (!perfil.isTieneTransportePropio() && lugar.isRequiereTransportePropio()) {
                throw new TransportePropioRequeridoException(lugar.getId());
            }

            int ordenActual = planificadas.size() + 1;
            if (finAnterior != null) {
                LocalTime minimoInicio = finAnterior.plus(MARGEN_MINIMO_TRASLADO);
                if (solicitada.horaInicio().isBefore(minimoInicio)) {
                    throw new HorariosSolapadosException(
                            ordenAnterior, finAnterior, ordenActual, solicitada.horaInicio());
                }
            }
            if (!solicitada.horaFin().isAfter(solicitada.horaInicio())) {
                throw new HorariosSolapadosException(
                        ordenActual, solicitada.horaInicio(), ordenActual, solicitada.horaFin());
            }

            planificadas.add(new ParadaPlanificada(lugar, solicitada.horaInicio(), solicitada.horaFin()));
            finAnterior = solicitada.horaFin();
            ordenAnterior = ordenActual;
        }

        return planificadas;
    }
}
