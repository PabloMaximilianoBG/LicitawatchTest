package cl.licitawatch.usuarios.bd.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RubroRequest(@NotBlank @Size(max = 100) String nombre) {
}
