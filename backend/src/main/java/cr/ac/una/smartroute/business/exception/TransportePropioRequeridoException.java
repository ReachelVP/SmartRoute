package cr.ac.una.smartroute.business.exception;

/**
 * Regla del Proceso 2: "si el usuario no tiene transporte propio, todos los lugares elegidos deben
 * ser accesibles entre sí sin transporte propio".
 */
public class TransportePropioRequeridoException extends RuntimeException {

    public TransportePropioRequeridoException(Long lugarId) {
        super("El lugar " + lugarId + " requiere transporte propio, pero el usuario no tiene.");
    }
}
