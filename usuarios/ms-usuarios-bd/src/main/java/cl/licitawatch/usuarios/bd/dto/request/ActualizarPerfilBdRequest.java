package cl.licitawatch.usuarios.bd.dto.request;

import jakarta.validation.Valid;
import lombok.Builder;

/** Datos editables del perfil del rol actual (rut y email de acceso no se tocan). */
@Builder
public record ActualizarPerfilBdRequest(@Valid DatosEmpresaBdRequest empresa, @Valid DatosAdministradorBdRequest administrador) {
}
