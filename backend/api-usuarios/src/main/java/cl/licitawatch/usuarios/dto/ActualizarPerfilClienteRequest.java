package cl.licitawatch.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarPerfilClienteRequest(
        @NotBlank @Size(max = 200) String nombreContacto
) {
}
