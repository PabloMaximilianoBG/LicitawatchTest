package cl.licitawatch.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Base de las excepciones propias: cada una define su código HTTP y un código de negocio legible. */
@Getter
public abstract class LicitaWatchException extends RuntimeException {
    private final HttpStatus status;
    private final String codigo;

    protected LicitaWatchException(HttpStatus status, String codigo, String mensaje) {
        super(mensaje);
        this.status = status;
        this.codigo = codigo;
    }
}
