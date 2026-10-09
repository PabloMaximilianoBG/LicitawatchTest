package cl.licitawatch.notificaciones.bs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/** Evento enviado por Licitaciones, Ventas o Chat. tipo = nombre exacto del catálogo tipo_notificacion. */
public record NotificacionRequest(@NotNull Integer usuarioId, @NotBlank String tipo, Map<String, String> datos) {
}
