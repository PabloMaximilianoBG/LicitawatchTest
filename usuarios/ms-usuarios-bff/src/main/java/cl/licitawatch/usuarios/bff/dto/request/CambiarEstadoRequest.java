package cl.licitawatch.usuarios.bff.dto.request;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(@NotNull Boolean activo) {
}
