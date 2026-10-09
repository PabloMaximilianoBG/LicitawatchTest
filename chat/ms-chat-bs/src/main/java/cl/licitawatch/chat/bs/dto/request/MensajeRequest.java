package cl.licitawatch.chat.bs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Versión MVP: solo texto (ER pág. 6). */
public record MensajeRequest(@NotBlank(message = "Escribe un mensaje") @Size(max = 2000, message = "Máximo 2000 caracteres") String contenido) {
}
