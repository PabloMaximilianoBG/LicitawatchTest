package cl.licitawatch.usuarios.bd.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/** Crea el perfil de un rol para un usuario existente (cambio de rol desde el panel de administración). */
@Builder
public record CrearPerfilBdRequest(@NotBlank String rolNombre, @Valid DatosEmpresaBdRequest empresa,
                                   @Valid DatosAdministradorBdRequest administrador) {
}
