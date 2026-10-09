package cl.licitawatch.ventas.bd.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VentaBdRequest(@NotNull Integer suscripcionId, @NotNull @PositiveOrZero BigDecimal monto, @NotNull LocalDate fecha) {
}
