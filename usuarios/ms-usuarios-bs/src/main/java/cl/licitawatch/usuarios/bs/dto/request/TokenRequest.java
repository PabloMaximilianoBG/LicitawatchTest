package cl.licitawatch.usuarios.bs.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(@NotBlank(message = "El token es obligatorio") String token) {
}
