package cl.licitawatch.usuarios.bff.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RubroRequest(@NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre) {
}
