package cl.licitawatch.usuarios.exception;

import org.springframework.http.HttpStatus;

public class EmailYaRegistradoException extends ApiException {
    public EmailYaRegistradoException(String email) {
        super(HttpStatus.CONFLICT, "El email '" + email + "' ya esta registrado");
    }
}
