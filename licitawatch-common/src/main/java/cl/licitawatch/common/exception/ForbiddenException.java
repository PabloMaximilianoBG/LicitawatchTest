package cl.licitawatch.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends LicitaWatchException {
    public ForbiddenException(String mensaje) {
        super(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", mensaje);
    }

    public ForbiddenException(String codigo, String mensaje) {
        super(HttpStatus.FORBIDDEN, codigo, mensaje);
    }
}
