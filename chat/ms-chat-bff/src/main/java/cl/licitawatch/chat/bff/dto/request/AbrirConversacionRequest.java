package cl.licitawatch.chat.bff.dto.request;

import jakarta.validation.constraints.NotNull;

public record AbrirConversacionRequest(@NotNull(message = "Indica la postulación") Integer postulacionId) {
}
