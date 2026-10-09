package cl.licitawatch.chat.bs.dto.request;

import jakarta.validation.constraints.NotNull;

public record AbrirConversacionRequest(@NotNull(message = "Indica la postulación") Integer postulacionId) {
}
