package cl.licitawatch.chat.bs.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ConversacionResponse(Integer id, Integer postulacionId, Integer licitacionId, String licitacionTitulo,
                                   Integer licitadorId, String licitadorNombre, Integer pymeId, String pymeNombre,
                                   boolean pymePremium, String contraparteNombre, LocalDateTime createdAt, String ultimoMensaje,
                                   LocalDateTime ultimoMensajeAt, long noLeidos) {
}
