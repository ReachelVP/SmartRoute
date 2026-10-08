package cr.ac.una.smartroute.business.exception;

/** Correo o contraseña incorrectos en {@code POST /auth/login}. */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos.");
    }
}
