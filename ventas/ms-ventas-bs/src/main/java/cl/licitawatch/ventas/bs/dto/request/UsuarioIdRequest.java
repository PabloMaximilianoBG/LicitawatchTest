package cl.licitawatch.ventas.bs.dto.request;

import jakarta.validation.constraints.NotNull;

public record UsuarioIdRequest(@NotNull Integer usuarioId) {
}
