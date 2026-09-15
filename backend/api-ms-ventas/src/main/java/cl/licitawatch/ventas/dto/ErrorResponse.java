package cl.licitawatch.ventas.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        int status,
        String mensaje,
        String path,
        LocalDateTime timestamp,
        List<String> detalles
) {
}
