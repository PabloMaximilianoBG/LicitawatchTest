package cl.licitawatch.usuarios.bs.dto.request;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(@NotNull Boolean activo) {
}
