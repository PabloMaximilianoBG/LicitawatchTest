package cl.licitawatch.notificaciones.bd.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificacionBdRequest(@NotNull Integer usuarioId, @NotBlank String tipo, @NotBlank String canal) {
}
