package cl.licitawatch.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends LicitaWatchException {
    public UnauthorizedException(String mensaje) {
        super(HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO", mensaje);
    }

    public UnauthorizedException(String codigo, String mensaje) {
        super(HttpStatus.UNAUTHORIZED, codigo, mensaje);
    }
}
