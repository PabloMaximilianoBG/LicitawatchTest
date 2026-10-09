package cl.licitawatch.usuarios.bd.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/** usuario + su perfil (según rolNombre) en una sola transacción. */
@Builder
public record CrearUsuarioBdRequest(@NotBlank String email, @NotBlank String password, @NotBlank String rolNombre,
                                    @NotNull Boolean activo, @Valid DatosEmpresaBdRequest empresa,
                                    @Valid DatosAdministradorBdRequest administrador) {
}
