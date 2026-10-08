package cr.ac.una.smartroute.business.exception;

public class ResenaInexistenteException extends RuntimeException {

    public ResenaInexistenteException(Long id) {
        super("La reseña " + id + " no existe.");
    }
}
