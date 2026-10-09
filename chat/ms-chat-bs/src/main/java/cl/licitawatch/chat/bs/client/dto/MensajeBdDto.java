package cl.licitawatch.chat.bs.client.dto;

import java.time.LocalDateTime;

public record MensajeBdDto(Integer id, Integer conversacionId, Integer emisorId, String contenido, LocalDateTime enviadoAt, Boolean leido) {
}
