package cl.licitawatch.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends LicitaWatchException {
    public ConflictException(String mensaje) {
        super(HttpStatus.CONFLICT, "CONFLICTO", mensaje);
    }

    public ConflictException(String codigo, String mensaje) {
        super(HttpStatus.CONFLICT, codigo, mensaje);
    }
}
