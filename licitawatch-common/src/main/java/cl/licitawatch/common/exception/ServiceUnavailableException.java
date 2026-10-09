package cl.licitawatch.common.exception;

import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends LicitaWatchException {
    public ServiceUnavailableException(String mensaje) {
        super(HttpStatus.SERVICE_UNAVAILABLE, "SERVICIO_NO_DISPONIBLE", mensaje);
    }

    public ServiceUnavailableException(String codigo, String mensaje) {
        super(HttpStatus.SERVICE_UNAVAILABLE, codigo, mensaje);
    }
}
