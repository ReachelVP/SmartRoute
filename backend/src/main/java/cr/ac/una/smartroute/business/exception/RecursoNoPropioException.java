package cr.ac.una.smartroute.business.exception;

/**
 * El usuario autenticado pidió un recurso que existe, pero que no le pertenece (y no es ADMIN).
 * Verificación de propiedad del recurso en el servicio — OWASP API1: Broken Object Level
 * Authorization.
 */
public class RecursoNoPropioException extends RuntimeException {

    public RecursoNoPropioException(String mensaje) {
        super(mensaje);
    }
}
