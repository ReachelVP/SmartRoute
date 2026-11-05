package cr.ac.una.smartroute.config.security;

import cr.ac.una.smartroute.business.exception.DemasiadosIntentosException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Laboratorio 7 — auditoría de seguridad (OWASP Top 10:2025, A06/A07): antes de esto,
 * {@code POST /auth/login} aceptaba cualquier cantidad de intentos para el mismo correo sin
 * límite — fuerza bruta de contraseña viable. Bloquea un correo por {@value #MINUTOS_VENTANA}
 * minutos después de {@value #MAX_INTENTOS} fallos seguidos.
 *
 * <p>Contador en memoria, por instancia del proceso -- suficiente para el despliegue actual (una
 * sola instancia). Con varias instancias detrás de un balanceador, este conteo no se comparte
 * entre ellas; para eso haría falta un almacén centralizado (p. ej. Redis), fuera del alcance de
 * este laboratorio.
 */
@Component
public class LimitadorIntentosLogin {

    private static final int MAX_INTENTOS = 5;
    private static final int MINUTOS_VENTANA = 15;

    private final ConcurrentHashMap<String, Intentos> porCorreo = new ConcurrentHashMap<>();

    public void verificarNoBloqueado(String correo) {
        Intentos intentos = porCorreo.get(normalizar(correo));
        if (intentos != null && intentos.estaBloqueado()) {
            throw new DemasiadosIntentosException();
        }
    }

    public void registrarFallo(String correo) {
        porCorreo.compute(normalizar(correo), (c, actual) -> {
            if (actual == null || actual.ventanaExpirada()) {
                return new Intentos(1, Instant.now());
            }
            return actual.incrementar();
        });
    }

    public void registrarExito(String correo) {
        porCorreo.remove(normalizar(correo));
    }

    private static String normalizar(String correo) {
        return correo == null ? "" : correo.toLowerCase(Locale.ROOT);
    }

    private record Intentos(int conteo, Instant primerIntento) {
        boolean ventanaExpirada() {
            return Instant.now().isAfter(primerIntento.plus(Duration.ofMinutes(MINUTOS_VENTANA)));
        }

        boolean estaBloqueado() {
            return conteo >= MAX_INTENTOS && !ventanaExpirada();
        }

        Intentos incrementar() {
            return new Intentos(conteo + 1, primerIntento);
        }
    }
}
