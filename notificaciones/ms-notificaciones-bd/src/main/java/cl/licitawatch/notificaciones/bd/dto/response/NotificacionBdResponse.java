package cl.licitawatch.notificaciones.bd.dto.response;

import java.time.LocalDateTime;

public record NotificacionBdResponse(Integer id, Integer usuarioId, String tipo, String canal, LocalDateTime createdAt) {
}
