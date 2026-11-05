package cr.ac.una.smartroute.config.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cr.ac.una.smartroute.business.exception.DemasiadosIntentosException;
import org.junit.jupiter.api.Test;

/** Laboratorio 7: regla de negocio de seguridad detrás del límite de intentos de login. */
class LimitadorIntentosLoginTest {

    @Test
    void bloqueaElCorreo_trasCincoFallosSeguidos() {
        LimitadorIntentosLogin limitador = new LimitadorIntentosLogin();

        for (int i = 0; i < 5; i++) {
            limitador.registrarFallo("ataque@ejemplo.com");
        }

        assertThrows(DemasiadosIntentosException.class, () -> limitador.verificarNoBloqueado("ataque@ejemplo.com"));
    }

    @Test
    void noBloquea_antesDeLlegarAlLimite() {
        LimitadorIntentosLogin limitador = new LimitadorIntentosLogin();

        for (int i = 0; i < 4; i++) {
            limitador.registrarFallo("casi@ejemplo.com");
        }

        assertDoesNotThrow(() -> limitador.verificarNoBloqueado("casi@ejemplo.com"));
    }

    @Test
    void unLoginExitoso_reiniciaElContador() {
        LimitadorIntentosLogin limitador = new LimitadorIntentosLogin();

        for (int i = 0; i < 4; i++) {
            limitador.registrarFallo("recupera@ejemplo.com");
        }
        limitador.registrarExito("recupera@ejemplo.com");

        // Si no se hubiera reiniciado, este quinto fallo ya activaría el bloqueo.
        limitador.registrarFallo("recupera@ejemplo.com");
        assertDoesNotThrow(() -> limitador.verificarNoBloqueado("recupera@ejemplo.com"));
    }

    @Test
    void elBloqueoEsPorCorreo_noGlobal() {
        LimitadorIntentosLogin limitador = new LimitadorIntentosLogin();

        for (int i = 0; i < 5; i++) {
            limitador.registrarFallo("victima@ejemplo.com");
        }

        assertDoesNotThrow(() -> limitador.verificarNoBloqueado("otra-persona@ejemplo.com"));
    }

    @Test
    void laComparacionDeCorreo_noDistingueMayusculas() {
        LimitadorIntentosLogin limitador = new LimitadorIntentosLogin();

        for (int i = 0; i < 5; i++) {
            limitador.registrarFallo("Mayus@Ejemplo.com");
        }

        assertThrows(DemasiadosIntentosException.class, () -> limitador.verificarNoBloqueado("mayus@ejemplo.com"));
    }
}
