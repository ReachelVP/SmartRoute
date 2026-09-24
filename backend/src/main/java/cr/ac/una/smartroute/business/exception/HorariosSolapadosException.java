package cr.ac.una.smartroute.business.exception;

import java.time.LocalTime;

/**
 * Regla del Proceso 2: "la hora de inicio de una parada debe ser posterior a la hora de fin de la
 * anterior, dejando además un margen mínimo de traslado entre lugares".
 */
public class HorariosSolapadosException extends RuntimeException {

    public HorariosSolapadosException(
            int ordenAnterior, LocalTime finAnterior, int ordenSiguiente, LocalTime inicioSiguiente) {
        super("La parada " + ordenSiguiente + " empieza a las " + inicioSiguiente
                + ", antes de que la parada " + ordenAnterior + " termine (" + finAnterior
                + ") más el margen mínimo de traslado.");
    }
}
