package cl.licitawatch.licitaciones.exception;

import org.springframework.http.HttpStatus;

public class PostulacionDuplicadaException extends ApiException {
    public PostulacionDuplicadaException() {
        super(HttpStatus.CONFLICT, "Ya postulaste a esta licitacion");
    }
}
