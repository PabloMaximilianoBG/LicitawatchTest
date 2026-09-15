package cl.licitawatch.ventas.dto;

import jakarta.validation.constraints.NotNull;

public record IniciarVentaRequest(
        @NotNull Long planId
) {
}
