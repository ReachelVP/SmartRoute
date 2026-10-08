package cr.ac.una.smartroute.business.exception;

public class ItinerarioInexistenteException extends RuntimeException {

    public ItinerarioInexistenteException(Long id) {
        super("El itinerario " + id + " no existe.");
    }
}
