package cl.licitawatch.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends LicitaWatchException {
    public BadRequestException(String mensaje) {
        super(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA", mensaje);
    }

    public BadRequestException(String codigo, String mensaje) {
        super(HttpStatus.BAD_REQUEST, codigo, mensaje);
    }
}
