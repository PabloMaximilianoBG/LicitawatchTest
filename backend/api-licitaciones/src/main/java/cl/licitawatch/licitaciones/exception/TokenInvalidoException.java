package cl.licitawatch.licitaciones.exception;

import org.springframework.http.HttpStatus;

public class TokenInvalidoException extends ApiException {
    public TokenInvalidoException(String mensaje) {
        super(HttpStatus.UNAUTHORIZED, mensaje);
    }
}
