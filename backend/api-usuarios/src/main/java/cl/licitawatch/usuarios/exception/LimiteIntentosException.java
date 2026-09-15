package cl.licitawatch.usuarios.exception;

import org.springframework.http.HttpStatus;

public class LimiteIntentosException extends ApiException {
    public LimiteIntentosException() {
        super(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos de inicio de sesion, intenta nuevamente en un minuto");
    }
}
