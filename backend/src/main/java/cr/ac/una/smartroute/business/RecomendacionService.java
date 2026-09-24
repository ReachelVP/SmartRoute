package cr.ac.una.smartroute.business;

import cr.ac.una.smartroute.business.exception.PerfilPreferenciasIncompletoException;
import cr.ac.una.smartroute.data.entity.Categoria;
import cr.ac.una.smartroute.data.entity.Lugar;
import cr.ac.una.smartroute.data.entity.PerfilPreferencias;
import cr.ac.una.smartroute.data.repository.LugarRepository;
import cr.ac.una.smartroute.data.repository.PerfilPreferenciasRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso completo del Proceso 1 del dominio: recibe el DTO validado (Bean Validation, en el
 * controlador), aplica la validación de negocio (perfil de preferencias completo) y las reglas
 * (los cuatro filtros de {@link RecomendacionFilters}) orquestando dos repositorios.
 */
@Service
public class RecomendacionService {

    private final LugarRepository lugarRepository;
    private final PerfilPreferenciasRepository perfilPreferenciasRepository;

    public RecomendacionService(
            LugarRepository lugarRepository, PerfilPreferenciasRepository perfilPreferenciasRepository) {
        this.lugarRepository = lugarRepository;
        this.perfilPreferenciasRepository = perfilPreferenciasRepository;
    }

    @Transactional(readOnly = true)
    public ResultadoRecomendacion recomendar(Long usuarioId, Long provinciaId) {
        PerfilPreferencias perfil = perfilPreferenciasRepository
                .findByUsuarioId(usuarioId)
                .orElseThrow(() -> new PerfilPreferenciasIncompletoException(usuarioId));

        CriteriosRecomendacion criterios = new CriteriosRecomendacion(
                provinciaId,
                categoriaIdsDe(perfil),
                perfil.getPresupuestoMaximo(),
                perfil.isTieneTransportePropio());

        List<Lugar> catalogo = lugarRepository.findAll().stream().filter(Lugar::isActivo).toList();
        return RecomendacionFilters.aplicar(catalogo, criterios);
    }

    private static Set<Long> categoriaIdsDe(PerfilPreferencias perfil) {
        return perfil.getCategoriasInteres().stream().map(Categoria::getId).collect(Collectors.toSet());
    }
}
