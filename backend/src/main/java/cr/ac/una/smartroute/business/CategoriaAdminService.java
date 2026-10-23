package cr.ac.una.smartroute.business;

import cr.ac.una.smartroute.business.exception.CategoriaInexistenteException;
import cr.ac.una.smartroute.data.entity.Categoria;
import cr.ac.una.smartroute.data.repository.CategoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Altas, bajas y ediciones del catálogo de categorías — segundo caso de uso ADMIN (Lab 6). */
@Service
public class CategoriaAdminService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaAdminService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public Page<Categoria> listarPaginado(Pageable pageable) {
        return categoriaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Categoria obtener(Long id) {
        return categoriaRepository.findById(id).orElseThrow(() -> new CategoriaInexistenteException(id));
    }

    @Transactional
    public Categoria crear(String nombre, String descripcion) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria actualizar(Long id, String nombre, String descripcion) {
        Categoria categoria = obtener(id);
        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);
        return categoriaRepository.save(categoria);
    }
}
