package cl.licitawatch.ventas.bd.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EstadoRequest(@NotBlank String estado) {
}
