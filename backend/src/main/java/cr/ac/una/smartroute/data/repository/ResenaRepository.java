package cr.ac.una.smartroute.data.repository;

import cr.ac.una.smartroute.data.entity.Resena;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResenaRepository extends BaseRepository<Resena, Long> {

    /**
     * Consulta de negocio (JPQL, 1 de 2): promedio de calificación de un lugar. Es la razón por la
     * que las reseñas se quedaron en SQL Server en vez de moverse a MongoDB (Lab 2) — necesitan
     * agregación, algo que el modelo relacional resuelve mejor que un documento.
     */
    @Query("SELECT AVG(r.calificacion) FROM Resena r WHERE r.lugar.id = :lugarId")
    Optional<Double> promedioCalificacionPorLugar(@Param("lugarId") Long lugarId);
}
