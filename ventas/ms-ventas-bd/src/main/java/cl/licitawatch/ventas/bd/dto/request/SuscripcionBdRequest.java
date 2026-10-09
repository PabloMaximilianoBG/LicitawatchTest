package cl.licitawatch.ventas.bd.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SuscripcionBdRequest(@NotNull Integer usuarioId, @NotBlank String plan, @NotBlank String estado) {
}
