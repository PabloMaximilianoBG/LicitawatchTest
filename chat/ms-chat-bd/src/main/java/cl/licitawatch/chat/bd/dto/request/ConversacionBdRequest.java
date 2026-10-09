package cl.licitawatch.chat.bd.dto.request;

import jakarta.validation.constraints.NotNull;

public record ConversacionBdRequest(@NotNull Integer postulacionId, @NotNull Integer licitadorId, @NotNull Integer pymeId) {
}
