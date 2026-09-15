package cl.licitawatch.licitaciones.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CrearLicitacionRequest(
        @NotBlank @Size(max = 200) String titulo,
        @NotBlank @Size(max = 100) String rubro,
        @DecimalMin(value = "0", inclusive = true) BigDecimal montoEstimado,
        @NotBlank @Size(max = 100) String region,
        @NotNull @FutureOrPresent LocalDate fechaCierre
) {
}
