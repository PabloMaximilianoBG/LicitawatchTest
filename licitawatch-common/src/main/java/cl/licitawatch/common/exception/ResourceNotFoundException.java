package cl.licitawatch.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends LicitaWatchException {
    public ResourceNotFoundException(String mensaje) {
        super(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", mensaje);
    }

    public ResourceNotFoundException(String codigo, String mensaje) {
        super(HttpStatus.NOT_FOUND, codigo, mensaje);
    }
}
