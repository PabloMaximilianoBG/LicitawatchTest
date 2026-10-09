package cl.licitawatch.ventas.ambassador.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CrearTransaccionRequest(@NotBlank @Size(max = 26) String ordenCompra, @NotBlank @Size(max = 61) String sesionId,
                                      @NotNull @Positive BigDecimal monto, @NotBlank String urlRetorno) {
}
