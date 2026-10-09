package cl.licitawatch.common.exception;

import cl.licitawatch.common.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Manejo global de errores: respuestas JSON uniformes, sin stack traces. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LicitaWatchException.class)
    public ResponseEntity<ErrorResponse> propia(LicitaWatchException e, HttpServletRequest req) {
        return respuesta(e.getStatus().value(), e.getCodigo(), e.getMessage(), req, null);
    }

    @ExceptionHandler(RemoteServiceException.class)
    public ResponseEntity<ErrorResponse> remota(RemoteServiceException e, HttpServletRequest req) {
        Map<String, String> errores = e.getError() != null ? e.getError().errores() : null;
        int status = e.getStatus() >= 500 && e.getStatus() != 503 ? 502 : e.getStatus();
        return respuesta(status, e.codigo(), e.getMessage(), req, errores);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e, HttpServletRequest req) {
        Map<String, String> errores = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> errores.putIfAbsent(f.getField(), f.getDefaultMessage()));
        e.getBindingResult().getGlobalErrors().forEach(g -> errores.putIfAbsent(g.getObjectName(), g.getDefaultMessage()));
        return respuesta(400, "VALIDACION", "Hay campos con errores", req, errores);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> restriccion(ConstraintViolationException e, HttpServletRequest req) {
        Map<String, String> errores = new LinkedHashMap<>();
        e.getConstraintViolations().forEach(v -> errores.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
        return respuesta(400, "VALIDACION", "Hay parámetros con errores", req, errores);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> validacionMetodo(HandlerMethodValidationException e, HttpServletRequest req) {
        return respuesta(400, "VALIDACION", "Hay parámetros con errores", req, null);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, MissingServletRequestPartException.class})
    public ResponseEntity<ErrorResponse> malformada(Exception e, HttpServletRequest req) {
        return respuesta(400, "SOLICITUD_INVALIDA", "La solicitud está incompleta o tiene un formato inválido", req, null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> tamano(MaxUploadSizeExceededException e, HttpServletRequest req) {
        return respuesta(400, "ARCHIVO_DEMASIADO_GRANDE", "El archivo supera el tamaño máximo permitido", req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodo(HttpRequestMethodNotSupportedException e, HttpServletRequest req) {
        return respuesta(405, "METODO_NO_PERMITIDO", "Método HTTP no permitido", req, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> mediaType(HttpMediaTypeNotSupportedException e, HttpServletRequest req) {
        return respuesta(415, "TIPO_NO_SOPORTADO", "Tipo de contenido no soportado", req, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(NoResourceFoundException e, HttpServletRequest req) {
        return respuesta(404, "RUTA_NO_ENCONTRADA", "Recurso no encontrado", req, null);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> servicioCaido(ResourceAccessException e, HttpServletRequest req) {
        log.error("Servicio interno no disponible: {}", e.getMessage());
        return respuesta(503, "SERVICIO_NO_DISPONIBLE", "Un servicio interno no está disponible. Intenta nuevamente en unos minutos.", req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> generico(Exception e, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), e);
        return respuesta(500, "ERROR_INTERNO", "Ocurrió un error inesperado", req, null);
    }

    private static ResponseEntity<ErrorResponse> respuesta(int status, String codigo, String mensaje,
                                                           HttpServletRequest req, Map<String, String> errores) {
        ErrorResponse body = ErrorResponse.builder().timestamp(OffsetDateTime.now()).status(status).codigo(codigo)
                .mensaje(mensaje).ruta(req.getRequestURI()).errores(errores == null || errores.isEmpty() ? null : errores).build();
        return ResponseEntity.status(HttpStatus.valueOf(status)).body(body);
    }
}
