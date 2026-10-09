package cl.licitawatch.chat.bd.dto.response;

import java.time.LocalDateTime;

public record MensajeBdResponse(Integer id, Integer conversacionId, Integer emisorId, String contenido, LocalDateTime enviadoAt,
                                Boolean leido) {
}
