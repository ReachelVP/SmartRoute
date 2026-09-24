package cr.ac.una.smartroute.presentation;

import cr.ac.una.smartroute.business.exception.HorariosSolapadosException;
import cr.ac.una.smartroute.business.exception.LugarInexistenteOInactivoException;
import cr.ac.una.smartroute.business.exception.PerfilPreferenciasIncompletoException;
import cr.ac.una.smartroute.business.exception.PresupuestoExcedidoException;
import cr.ac.una.smartroute.business.exception.TransportePropioRequeridoException;
import cr.ac.una.smartroute.presentation.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce las excepciones de negocio a respuestas HTTP con un cuerpo consistente. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PerfilPreferenciasIncompletoException.class)
    public ResponseEntity<ErrorResponse> perfilIncompleto(PerfilPreferenciasIncompletoException ex) {
        return respuesta(HttpStatus.UNPROCESSABLE_CONTENT, "perfil_incompleto", ex);
    }

    @ExceptionHandler(LugarInexistenteOInactivoException.class)
    public ResponseEntity<ErrorResponse> lugarInvalido(LugarInexistenteOInactivoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, "lugar_invalido", ex);
    }

    @ExceptionHandler(HorariosSolapadosException.class)
    public ResponseEntity<ErrorResponse> horariosSolapados(HorariosSolapadosException ex) {
        return respuesta(HttpStatus.CONFLICT, "horarios_solapados", ex);
    }

    @ExceptionHandler(TransportePropioRequeridoException.class)
    public ResponseEntity<ErrorResponse> transporteRequerido(TransportePropioRequeridoException ex) {
        return respuesta(HttpStatus.CONFLICT, "transporte_propio_requerido", ex);
    }

    @ExceptionHandler(PresupuestoExcedidoException.class)
    public ResponseEntity<ErrorResponse> presupuestoExcedido(PresupuestoExcedidoException ex) {
        return respuesta(HttpStatus.CONFLICT, "presupuesto_excedido", ex);
    }

    private static ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String codigo, Exception ex) {
        return ResponseEntity.status(status).body(new ErrorResponse(codigo, ex.getMessage()));
    }
}
