package cl.licitawatch.asistente.exception;

import org.springframework.http.HttpStatus;

public class AsistenteNoDisponibleException extends ApiException {
    public AsistenteNoDisponibleException(Throwable causa) {
        super(HttpStatus.BAD_GATEWAY, "LicitAsist no esta disponible en este momento, intenta nuevamente mas tarde.");
        initCause(causa);
    }
}
