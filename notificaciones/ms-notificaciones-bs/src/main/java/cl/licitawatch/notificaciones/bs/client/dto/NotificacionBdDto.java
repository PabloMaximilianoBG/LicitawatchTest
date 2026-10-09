package cl.licitawatch.notificaciones.bs.client.dto;

import java.time.LocalDateTime;

public record NotificacionBdDto(Integer id, Integer usuarioId, String tipo, String canal, LocalDateTime createdAt) {
}
