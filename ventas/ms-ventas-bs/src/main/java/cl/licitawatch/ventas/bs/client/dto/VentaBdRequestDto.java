package cl.licitawatch.ventas.bs.client.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VentaBdRequestDto(Integer suscripcionId, BigDecimal monto, LocalDate fecha) {
}
