package cr.ac.una.smartroute.presentation;

import cr.ac.una.smartroute.business.exception.CredencialesInvalidasException;
import cr.ac.una.smartroute.business.exception.HorariosSolapadosException;
import cr.ac.una.smartroute.business.exception.ItinerarioInexistenteException;
import cr.ac.una.smartroute.business.exception.LugarInexistenteOInactivoException;
import cr.ac.una.smartroute.business.exception.PerfilPreferenciasIncompletoException;
import cr.ac.una.smartroute.business.exception.PresupuestoExcedidoException;
import cr.ac.una.smartroute.business.exception.RecursoNoPropioException;
import cr.ac.una.smartroute.business.exception.ResenaDuplicadaException;
import cr.ac.una.smartroute.business.exception.ResenaInexistenteException;
import cr.ac.una.smartroute.business.exception.TransportePropioRequeridoException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce cada excepción de negocio a Problem Details (RFC 9457) — un único manejador global, para
 * toda la API (heredada y {@code /api/v1}). Ninguna traza de excepción llega al cliente: el
 * catch-all registra el detalle en el log del servidor y devuelve un mensaje genérico.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PerfilPreferenciasIncompletoException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ProblemDetail perfilIncompleto(PerfilPreferenciasIncompletoException ex) {
        return problema(HttpStatus.UNPROCESSABLE_CONTENT, "Perfil de preferencias incompleto", ex.getMessage());
    }

    @ExceptionHandler(TransportePropioRequeridoException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ProblemDetail transporteRequerido(TransportePropioRequeridoException ex) {
        return problema(HttpStatus.UNPROCESSABLE_CONTENT, "Transporte propio requerido", ex.getMessage());
    }

    @ExceptionHandler(HorariosSolapadosException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ProblemDetail horariosSolapados(HorariosSolapadosException ex) {
        return problema(HttpStatus.UNPROCESSABLE_CONTENT, "Horarios solapados", ex.getMessage());
    }

    @ExceptionHandler(PresupuestoExcedidoException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ProblemDetail presupuestoExcedido(PresupuestoExcedidoException ex) {
        return problema(HttpStatus.UNPROCESSABLE_CONTENT, "Presupuesto excedido", ex.getMessage());
    }

    @ExceptionHandler(LugarInexistenteOInactivoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail lugarInvalido(LugarInexistenteOInactivoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Lugar no encontrado", ex.getMessage());
    }

    @ExceptionHandler(ItinerarioInexistenteException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail itinerarioInexistente(ItinerarioInexistenteException ex) {
        return problema(HttpStatus.NOT_FOUND, "Itinerario no encontrado", ex.getMessage());
    }

    @ExceptionHandler(ResenaInexistenteException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail resenaInexistente(ResenaInexistenteException ex) {
        return problema(HttpStatus.NOT_FOUND, "Reseña no encontrada", ex.getMessage());
    }

    @ExceptionHandler(ResenaDuplicadaException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail resenaDuplicada(ResenaDuplicadaException ex) {
        return problema(HttpStatus.CONFLICT, "Reseña duplicada", ex.getMessage());
    }

    @ExceptionHandler(RecursoNoPropioException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ProblemDetail recursoNoPropio(RecursoNoPropioException ex) {
        return problema(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail credencialesInvalidas(CredencialesInvalidasException ex) {
        return problema(HttpStatus.UNAUTHORIZED, "No autenticado", ex.getMessage());
    }

    /** Referencias inválidas en el cuerpo de la petición (p. ej. provinciaId/categoriaId inexistentes). */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail solicitudInvalida(IllegalArgumentException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Solicitud inválida", ex.getMessage());
    }

    /** Bean Validation ({@code @Valid}) sobre los DTOs de entrada. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail formatoInvalido(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Validación de formato", "Uno o más campos no son válidos.");
        problema.setProperty("errores", errores);
        return problema;
    }

    /** Nunca se expone una traza al cliente; el detalle queda en el log del servidor. */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail errorInesperado(Exception ex) {
        log.error("Error inesperado procesando la petición", ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrió un error inesperado.");
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalle);
        problema.setTitle(titulo);
        return problema;
    }
}
