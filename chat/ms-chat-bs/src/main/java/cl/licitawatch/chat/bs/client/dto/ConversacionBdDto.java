package cl.licitawatch.chat.bs.client.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ConversacionBdDto(Integer id, Integer postulacionId, Integer licitadorId, Integer pymeId, LocalDateTime createdAt,
                                String ultimoMensaje, LocalDateTime ultimoMensajeAt, long noLeidos) {
}
