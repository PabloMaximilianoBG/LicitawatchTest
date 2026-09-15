package cl.licitawatch.usuarios.exception;

import org.springframework.http.HttpStatus;

public class RutYaRegistradoException extends ApiException {
    public RutYaRegistradoException(String rut) {
        super(HttpStatus.CONFLICT, "El RUT '" + rut + "' ya esta registrado");
    }
}
