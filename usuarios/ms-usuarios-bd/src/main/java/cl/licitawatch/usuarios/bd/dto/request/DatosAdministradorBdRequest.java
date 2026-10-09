package cl.licitawatch.usuarios.bd.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record DatosAdministradorBdRequest(@NotBlank String nombre, @NotBlank String area) {
}
