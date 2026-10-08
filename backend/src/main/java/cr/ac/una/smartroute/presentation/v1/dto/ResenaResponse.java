package cr.ac.una.smartroute.presentation.v1.dto;

import cr.ac.una.smartroute.data.entity.Resena;
import java.time.LocalDateTime;

public record ResenaResponse(
        Long id, Long lugarId, String nombreLugar, Long usuarioId, short calificacion, String comentario, LocalDateTime creadoEn) {

    public static ResenaResponse from(Resena resena) {
        return new ResenaResponse(
                resena.getId(),
                resena.getLugar().getId(),
                resena.getLugar().getNombre(),
                resena.getUsuario().getId(),
                resena.getCalificacion(),
                resena.getComentario(),
                resena.getCreadoEn());
    }
}
