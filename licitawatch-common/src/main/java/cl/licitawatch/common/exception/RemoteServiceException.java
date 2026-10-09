package cl.licitawatch.common.exception;

import cl.licitawatch.common.error.ErrorResponse;
import lombok.Getter;

/** Error devuelto por otro microservicio: se propaga con el mismo código HTTP y mensaje. */
@Getter
public class RemoteServiceException extends RuntimeException {
    private final int status;
    private final transient ErrorResponse error;

    public RemoteServiceException(int status, ErrorResponse error) {
        super(error != null && error.mensaje() != null ? error.mensaje() : "Error en servicio remoto (" + status + ")");
        this.status = status;
        this.error = error;
    }

    public String codigo() {
        return error != null && error.codigo() != null ? error.codigo() : "ERROR_REMOTO";
    }

    public boolean esNoEncontrado() {
        return status == 404;
    }
}
