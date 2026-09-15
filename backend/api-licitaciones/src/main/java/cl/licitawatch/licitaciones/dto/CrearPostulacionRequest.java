package cl.licitawatch.licitaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearPostulacionRequest(
        @NotBlank @Size(max = 4000) String propuesta
) {
}
