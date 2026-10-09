package cl.licitawatch.notificaciones.bs.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CorreoEnlaceRequest(@NotBlank @Email String email, String nombre, @NotBlank String enlace) {
}
