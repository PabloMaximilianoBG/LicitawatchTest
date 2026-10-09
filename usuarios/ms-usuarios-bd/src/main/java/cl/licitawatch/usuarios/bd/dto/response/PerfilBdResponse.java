package cl.licitawatch.usuarios.bd.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

/** usuario + perfil de su rol con los catálogos resueltos (campos de otros roles quedan en null). */
@Builder
public record PerfilBdResponse(Integer usuarioId, String email, String password, String rolNombre, Boolean activo,
                               LocalDateTime createdAt, Integer perfilId,
                               String razonSocial, String rut, String nombreContacto, String emailContacto, String telefono,
                               Integer rubroId, String rubroNombre, Integer ciudadId, String ciudadNombre,
                               Integer regionId, String regionNombre, Integer tamanoEmpresaId, String tamanoEmpresaNombre,
                               String descripcionEmpresa, String sitioWeb, LocalDateTime updatedAt,
                               String nombre, String area) {
}
