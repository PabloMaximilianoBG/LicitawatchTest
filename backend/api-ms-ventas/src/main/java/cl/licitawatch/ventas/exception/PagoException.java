package cl.licitawatch.ventas.exception;

import org.springframework.http.HttpStatus;

public class PagoException extends ApiException {
    public PagoException(String mensaje, Throwable causa) {
        super(HttpStatus.BAD_GATEWAY, mensaje);
        initCause(causa);
    }
}
