package cl.licitawatch.usuarios.bs.client.dto;

import lombok.Builder;

@Builder
public record CrearUsuarioBdDto(String email, String password, String rolNombre, Boolean activo,
                                DatosEmpresaBdDto empresa, DatosAdministradorBdDto administrador) {
}
