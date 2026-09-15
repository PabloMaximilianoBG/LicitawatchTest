package cl.licitawatch.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarPerfilEmpresaRequest(
        @NotBlank @Size(max = 200) String razonSocial,
        @Size(max = 100) String rubro
) {
}
