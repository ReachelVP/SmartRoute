package cr.ac.una.smartroute.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import cr.ac.una.smartroute.data.entity.Lugar;
import cr.ac.una.smartroute.data.repository.LugarRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LugarServiceTest {

    @Mock
    private LugarRepository lugarRepository;

    @Test
    void listarPorProvincia_delegaAlRepositorio() {
        Lugar museo = new Lugar();
        museo.setId(3L);
        when(lugarRepository.findByProvinciaId(1L)).thenReturn(List.of(museo));

        LugarService service = new LugarService(lugarRepository);
        List<Lugar> resultado = service.listarPorProvincia(1L);

        assertEquals(List.of(museo), resultado);
    }
}
