package cl.licitawatch.ventas.exception;

import org.springframework.http.HttpStatus;

public class PlanGratuitoException extends ApiException {
    public PlanGratuitoException() {
        super(HttpStatus.CONFLICT, "Este plan no requiere pago: ya viene incluido por defecto en tu cuenta.");
    }
}
