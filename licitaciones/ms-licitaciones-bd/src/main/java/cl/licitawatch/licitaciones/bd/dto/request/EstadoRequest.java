package cl.licitawatch.licitaciones.bd.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EstadoRequest(@NotBlank String estado) {
}
