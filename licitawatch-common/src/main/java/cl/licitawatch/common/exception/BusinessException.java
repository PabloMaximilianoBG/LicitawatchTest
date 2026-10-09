package cl.licitawatch.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends LicitaWatchException {
    public BusinessException(String mensaje) {
        super(HttpStatus.CONFLICT, "REGLA_DE_NEGOCIO", mensaje);
    }

    public BusinessException(String codigo, String mensaje) {
        super(HttpStatus.CONFLICT, codigo, mensaje);
    }
}
