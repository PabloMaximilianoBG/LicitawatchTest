package cl.licitawatch.ventas.bs.client.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record VentaBdDto(Integer id, Integer suscripcionId, Integer usuarioId, String plan, String suscripcionEstado,
                         BigDecimal monto, LocalDate fecha, PagoBdDto pago) {
}
