package cl.licitawatch.ventas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VentaAdminResponse(
        Long id,
        Long usuarioId,
        String plan,
        BigDecimal monto,
        LocalDate fecha,
        String estadoPago
) {
}
