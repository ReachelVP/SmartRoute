package cr.ac.una.smartroute.business.exception;

public class CategoriaInexistenteException extends RuntimeException {

    public CategoriaInexistenteException(Long id) {
        super("La categoría " + id + " no existe.");
    }
}
