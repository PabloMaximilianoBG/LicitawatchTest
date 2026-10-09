package cl.licitawatch.ventas.bs.client.dto;

import java.util.Map;

public record NotificacionDto(Integer usuarioId, String tipo, Map<String, String> datos) {
}
