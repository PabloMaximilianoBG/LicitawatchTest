package cl.licitawatch.ventas.bd.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/** venta + su suscripción y su pago (pago = null mientras no se paga). */
@Builder
public record VentaBdResponse(Integer id, Integer suscripcionId, Integer usuarioId, String plan, String suscripcionEstado,
                              BigDecimal monto, LocalDate fecha, PagoBdResponse pago) {
}
