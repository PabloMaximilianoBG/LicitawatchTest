package cl.licitawatch.chat.bd.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ConversacionBdResponse(Integer id, Integer postulacionId, Integer licitadorId, Integer pymeId, LocalDateTime createdAt,
                                     String ultimoMensaje, LocalDateTime ultimoMensajeAt, long noLeidos) {
}
