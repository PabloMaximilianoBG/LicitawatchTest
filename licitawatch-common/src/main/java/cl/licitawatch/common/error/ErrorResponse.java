package cl.licitawatch.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.Map;

/** Respuesta de error uniforme de todos los microservicios. */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(OffsetDateTime timestamp, int status, String codigo, String mensaje, String ruta,
                            Map<String, String> errores) {
}
