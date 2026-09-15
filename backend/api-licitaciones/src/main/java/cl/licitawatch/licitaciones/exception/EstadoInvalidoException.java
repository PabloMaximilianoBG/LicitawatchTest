package cl.licitawatch.licitaciones.exception;

import org.springframework.http.HttpStatus;

public class EstadoInvalidoException extends ApiException {
    public EstadoInvalidoException(String mensaje) {
        super(HttpStatus.CONFLICT, mensaje);
    }
}
