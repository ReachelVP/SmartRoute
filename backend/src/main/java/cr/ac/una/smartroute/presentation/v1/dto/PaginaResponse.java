package cr.ac.una.smartroute.presentation.v1.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Envoltorio propio para colecciones paginadas, en vez de serializar {@code Page<T>} de Spring
 * directamente: evita acoplar el contrato público al tipo interno de Spring Data y deja explícitos
 * justo los metadatos que un cliente necesita. El mapeo de entidad a DTO ocurre en el servicio
 * (dentro de la transacción), nunca acá — así ninguna asociación LAZY revienta al serializar.
 */
public record PaginaResponse<T>(List<T> contenido, int pagina, int tamanio, long totalElementos, int totalPaginas) {

    public static <T> PaginaResponse<T> from(Page<T> pagina) {
        return new PaginaResponse<>(
                pagina.getContent(), pagina.getNumber(), pagina.getSize(), pagina.getTotalElements(), pagina.getTotalPages());
    }
}
