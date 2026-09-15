package cl.licitawatch.ventas.exception;

import org.springframework.http.HttpStatus;

public class OperacionNoPermitidaException extends ApiException {
    public OperacionNoPermitidaException(String mensaje) {
        super(HttpStatus.FORBIDDEN, mensaje);
    }
}
