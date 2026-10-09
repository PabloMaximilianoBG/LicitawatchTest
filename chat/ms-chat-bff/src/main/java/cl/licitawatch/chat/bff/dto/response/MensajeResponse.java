package cl.licitawatch.chat.bff.dto.response;

import java.time.LocalDateTime;

public record MensajeResponse(Integer id, Integer conversacionId, Integer emisorId, boolean propio, String contenido,
                              LocalDateTime enviadoAt, Boolean leido) {
}
