package cl.licitawatch.usuarios.bff.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailRequest(@NotBlank(message = "El correo es obligatorio") @Email(message = "Correo inválido") String email) {
}
