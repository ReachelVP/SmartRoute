package cr.ac.una.smartroute.business.exception;

/** El usuario ya reseñó este lugar (restricción {@code UNIQUE(usuario_id, lugar_id)}). */
public class ResenaDuplicadaException extends RuntimeException {

    public ResenaDuplicadaException(Long lugarId) {
        super("Ya existe una reseña tuya para el lugar " + lugarId + ".");
    }
}
