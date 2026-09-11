package cr.ac.una.smartroute.data.repository;

import cr.ac.una.smartroute.data.entity.Itinerario;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItinerarioRepository extends BaseRepository<Itinerario, Long> {

    /**
     * Consulta de negocio (JPQL, 2 de 2). El {@code JOIN FETCH} es, a la vez, la corrección del
     * problema N+1: trae cada itinerario confirmado del usuario junto con sus paradas en una sola
     * consulta, en vez de una consulta aparte por itinerario al acceder a {@code getParadas()}
     * (ver {@code ItinerarioRepositoryIT} y docs/consultas-negocio.md).
     */
    @Query(
            "SELECT DISTINCT i FROM Itinerario i JOIN FETCH i.paradas "
                    + "WHERE i.usuario.id = :usuarioId AND i.estado = 'CONFIRMADO' "
                    + "ORDER BY i.fecha DESC")
    List<Itinerario> itinerariosConfirmadosDeUsuario(@Param("usuarioId") Long usuarioId);
}
