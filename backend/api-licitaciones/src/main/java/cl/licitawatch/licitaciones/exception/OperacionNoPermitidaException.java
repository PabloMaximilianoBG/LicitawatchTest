package cl.licitawatch.licitaciones.exception;

import org.springframework.http.HttpStatus;

public class OperacionNoPermitidaException extends ApiException {
    public OperacionNoPermitidaException(String mensaje) {
        super(HttpStatus.FORBIDDEN, mensaje);
    }
}
