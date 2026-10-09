package cl.licitawatch.usuarios.bff.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(@NotBlank(message = "El token es obligatorio") String token) {
}
