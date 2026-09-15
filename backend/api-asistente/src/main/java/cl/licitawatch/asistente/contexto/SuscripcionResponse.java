package cl.licitawatch.asistente.contexto;

import java.time.LocalDate;

public record SuscripcionResponse(
        Long usuarioId,
        String plan,
        String estado,
        LocalDate fechaInicio,
        LocalDate fechaVencimiento
) {
}
