package cl.licitawatch.licitaciones.bs.client.dto;

import java.util.Map;

/** tipo = nombre exacto del catálogo tipo_notificacion. */
public record NotificacionDto(Integer usuarioId, String tipo, Map<String, String> datos) {
}
