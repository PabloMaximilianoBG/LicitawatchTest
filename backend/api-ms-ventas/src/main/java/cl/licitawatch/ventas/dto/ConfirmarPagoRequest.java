package cl.licitawatch.ventas.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmarPagoRequest(
        @NotBlank String tokenWs
) {
}
