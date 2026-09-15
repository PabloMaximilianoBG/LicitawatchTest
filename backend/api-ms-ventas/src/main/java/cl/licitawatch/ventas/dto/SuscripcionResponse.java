package cl.licitawatch.ventas.dto;

import java.time.LocalDate;

public record SuscripcionResponse(
        Long usuarioId,
        String plan,
        String estado,
        LocalDate fechaInicio,
        LocalDate fechaVencimiento
) {
}
