package cr.ac.una.smartroute.business.exception;

/**
 * Validación del Proceso 2: "todos los lugares referenciados deben existir y estar activos al
 * momento de confirmar".
 */
public class LugarInexistenteOInactivoException extends RuntimeException {

    public LugarInexistenteOInactivoException(Long lugarId) {
        super("El lugar " + lugarId + " no existe o ya no está activo.");
    }
}
