package cr.ac.una.smartroute.business;

import cr.ac.una.smartroute.business.exception.LugarInexistenteOInactivoException;
import cr.ac.una.smartroute.data.entity.Categoria;
import cr.ac.una.smartroute.data.entity.Lugar;
import cr.ac.una.smartroute.data.entity.Provincia;
import cr.ac.una.smartroute.data.repository.CategoriaRepository;
import cr.ac.una.smartroute.data.repository.LugarRepository;
import cr.ac.una.smartroute.data.repository.ProvinciaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Altas, bajas y ediciones del catálogo de lugares — operaciones de ADMIN (Lab 5). */
@Service
public class LugarAdminService {

    private final LugarRepository lugarRepository;
    private final ProvinciaRepository provinciaRepository;
    private final CategoriaRepository categoriaRepository;

    public LugarAdminService(
            LugarRepository lugarRepository,
            ProvinciaRepository provinciaRepository,
            CategoriaRepository categoriaRepository) {
        this.lugarRepository = lugarRepository;
        this.provinciaRepository = provinciaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public Page<Lugar> listarPaginado(Specification<Lugar> filtro, Pageable pageable) {
        return lugarRepository.findAll(filtro, pageable);
    }

    @Transactional(readOnly = true)
    public Lugar obtener(Long id) {
        return lugarRepository.findById(id).orElseThrow(() -> new LugarInexistenteOInactivoException(id));
    }

    /** Para poblar los <select> de provincia/categoría del formulario administrativo (Lab 6). */
    @Transactional(readOnly = true)
    public List<Provincia> listarProvincias() {
        return provinciaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    @Transactional
    public Lugar crear(
            String nombre,
            String descripcion,
            Long provinciaId,
            Long categoriaId,
            BigDecimal costoEstimado,
            String nivelAccesibilidad,
            boolean requiereTransportePropio) {
        Lugar lugar = new Lugar();
        aplicarCampos(lugar, nombre, descripcion, provinciaId, categoriaId, costoEstimado, nivelAccesibilidad, requiereTransportePropio);
        lugar.setActivo(true);
        lugar.setCreadoEn(LocalDateTime.now());
        return lugarRepository.save(lugar);
    }

    /**
     * {@code versionEsperada} es la versión que el cliente vio cuando cargó el formulario/GET —
     * puede que ya no sea la que está en la base. Importante: la entidad que se guarda acá es
     * {@code paraActualizar}, una instancia {@code new Lugar()} que <b>nunca pasó por el
     * EntityManager</b>, no la que devuelve {@code obtener(id)}. Si en cambio se reusara esa
     * última (ya *managed* dentro de esta misma transacción) y solo se le pisara el campo
     * {@code version} a mano, Hibernate igual generaría el UPDATE comparando contra la versión
     * con la que la cargó -- la que acaba de leer, siempre la más reciente -- y el pisado manual
     * no tendría ningún efecto (así se rompió la primera versión de este método). Una instancia
     * genuinamente *detached* fuerza a Spring Data a resolver {@code save()} como
     * {@code EntityManager.merge(...)}, que sí compara la versión de la entidad entrante contra la
     * actual en la base: si no coinciden, lanza
     * {@link org.springframework.dao.OptimisticLockingFailureException} (bloqueo optimista,
     * Laboratorio 7) -- {@code GlobalExceptionHandler} la traduce a 409.
     */
    @Transactional
    public Lugar actualizar(
            Long id,
            String nombre,
            String descripcion,
            Long provinciaId,
            Long categoriaId,
            BigDecimal costoEstimado,
            String nivelAccesibilidad,
            boolean requiereTransportePropio,
            Long versionEsperada) {
        Lugar existente = obtener(id);

        Lugar paraActualizar = new Lugar();
        paraActualizar.setId(id);
        paraActualizar.setActivo(existente.isActivo());
        paraActualizar.setCreadoEn(existente.getCreadoEn());
        aplicarCampos(
                paraActualizar, nombre, descripcion, provinciaId, categoriaId, costoEstimado, nivelAccesibilidad, requiereTransportePropio);
        paraActualizar.setVersion(versionEsperada);

        return lugarRepository.saveAndFlush(paraActualizar);
    }

    /** Baja lógica: {@code activo = false}, no se borra la fila (hay itinerarios/reseñas que la referencian). */
    @Transactional
    public void eliminar(Long id) {
        Lugar lugar = obtener(id);
        lugar.setActivo(false);
        lugarRepository.save(lugar);
    }

    private void aplicarCampos(
            Lugar lugar,
            String nombre,
            String descripcion,
            Long provinciaId,
            Long categoriaId,
            BigDecimal costoEstimado,
            String nivelAccesibilidad,
            boolean requiereTransportePropio) {
        Provincia provincia = provinciaRepository
                .findById(provinciaId)
                .orElseThrow(() -> new IllegalArgumentException("La provincia " + provinciaId + " no existe."));
        Categoria categoria = categoriaRepository
                .findById(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("La categoría " + categoriaId + " no existe."));

        lugar.setNombre(nombre);
        lugar.setDescripcion(descripcion);
        lugar.setProvincia(provincia);
        lugar.setCategoria(categoria);
        lugar.setCostoEstimado(costoEstimado);
        lugar.setNivelAccesibilidad(nivelAccesibilidad);
        lugar.setRequiereTransportePropio(requiereTransportePropio);
    }
}
