package cl.licitawatch.usuarios.exception;

import org.springframework.http.HttpStatus;

public class CredencialesInvalidasException extends ApiException {
    public CredencialesInvalidasException() {
        super(HttpStatus.UNAUTHORIZED, "Email o contrasena invalidos");
    }
}
