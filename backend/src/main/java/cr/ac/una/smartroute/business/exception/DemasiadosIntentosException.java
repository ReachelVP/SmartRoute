package cr.ac.una.smartroute.business.exception;

/**
 * Laboratorio 7 — auditoría de seguridad: demasiados intentos fallidos de login seguidos para el
 * mismo correo, en la ventana reciente ({@code LimitadorIntentosLogin}). OWASP Top 10:2025, A06
 * (Insecure Design) / A07 (Authentication Failures) — sin esto, {@code POST /auth/login} permitía
 * fuerza bruta ilimitada contra una cuenta.
 */
public class DemasiadosIntentosException extends RuntimeException {

    public DemasiadosIntentosException() {
        super("Demasiados intentos fallidos. Esperá unos minutos antes de volver a intentar.");
    }
}
