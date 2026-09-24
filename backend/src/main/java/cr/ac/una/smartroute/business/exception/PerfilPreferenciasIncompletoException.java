package cr.ac.una.smartroute.business.exception;

/**
 * El usuario debe tener un {@code PerfilPreferencias} completo antes de pedir una recomendación o
 * confirmar un itinerario (validación explícita del Proceso 1 del dominio).
 */
public class PerfilPreferenciasIncompletoException extends RuntimeException {

    public PerfilPreferenciasIncompletoException(Long usuarioId) {
        super("El usuario " + usuarioId + " no tiene un perfil de preferencias completo. "
                + "Debe completarlo antes de continuar.");
    }
}
