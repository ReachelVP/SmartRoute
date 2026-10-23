package cr.ac.una.smartroute.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cr.ac.una.smartroute.business.exception.CategoriaInexistenteException;
import cr.ac.una.smartroute.data.entity.Categoria;
import cr.ac.una.smartroute.data.repository.CategoriaRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class CategoriaAdminServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Test
    void obtener_devuelveLaCategoria_siExiste() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        CategoriaAdminService service = new CategoriaAdminService(categoriaRepository);

        assertEquals(categoria, service.obtener(1L));
    }

    @Test
    void obtener_lanzaExcepcion_siNoExiste() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        CategoriaAdminService service = new CategoriaAdminService(categoriaRepository);

        assertThrows(CategoriaInexistenteException.class, () -> service.obtener(99L));
    }

    @Test
    void crear_guardaConNombreYDescripcion() {
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoriaAdminService service = new CategoriaAdminService(categoriaRepository);
        Categoria creada = service.crear("Aventura", "Actividades al aire libre");

        assertEquals("Aventura", creada.getNombre());
        assertEquals("Actividades al aire libre", creada.getDescripcion());
    }

    @Test
    void actualizar_modificaLaCategoriaExistente() {
        Categoria existente = new Categoria();
        existente.setId(5L);
        existente.setNombre("Vieja");
        when(categoriaRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoriaAdminService service = new CategoriaAdminService(categoriaRepository);
        Categoria actualizada = service.actualizar(5L, "Nueva", "Descripción nueva");

        assertEquals("Nueva", actualizada.getNombre());
        assertEquals("Descripción nueva", actualizada.getDescripcion());
    }

    @Test
    void listarPaginado_delegaAlRepositorio() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(categoriaRepository.findAll(pageable)).thenReturn(Page.empty());

        CategoriaAdminService service = new CategoriaAdminService(categoriaRepository);
        service.listarPaginado(pageable);

        verify(categoriaRepository).findAll(pageable);
    }
}
