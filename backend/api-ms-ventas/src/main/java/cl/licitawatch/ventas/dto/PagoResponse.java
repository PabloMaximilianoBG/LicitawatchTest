package cl.licitawatch.ventas.dto;

import java.time.LocalDate;

public record PagoResponse(
        String estadoPago,
        String estadoSuscripcion,
        String plan,
        LocalDate fechaVencimiento
) {
}
