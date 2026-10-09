package cl.licitawatch.chat.bd.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MensajeBdRequest(@NotNull Integer emisorId, @NotBlank String contenido) {
}
