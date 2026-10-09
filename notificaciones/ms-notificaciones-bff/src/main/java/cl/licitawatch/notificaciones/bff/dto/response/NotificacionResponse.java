package cl.licitawatch.notificaciones.bff.dto.response;

import java.time.LocalDateTime;

public record NotificacionResponse(Integer id, Integer usuarioId, String usuarioEmail, String tipo, String canal,
                                   LocalDateTime createdAt, Boolean correoEnviado) {
}
