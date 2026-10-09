package cl.licitawatch.usuarios.bs.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

/** usuario + perfil con catálogos resueltos. rol = LICITADOR | PYME | ADMINISTRADOR. premium solo aplica a Pyme. */
@Builder
public record PerfilResponse(Integer usuarioId, String email, String rol, Boolean activo, String estadoCuenta,
                             LocalDateTime createdAt, Integer perfilId,
                             String razonSocial, String rut, String nombreContacto, String emailContacto, String telefono,
                             Integer rubroId, String rubroNombre, Integer ciudadId, String ciudadNombre,
                             Integer regionId, String regionNombre, Integer tamanoEmpresaId, String tamanoEmpresaNombre,
                             String descripcionEmpresa, String sitioWeb, LocalDateTime updatedAt,
                             String nombre, String area, boolean premium) {

    /** Nombre para mostrar: razón social (Licitador/Pyme) o nombre (Administrador). */
    public String nombreVisible() {
        return razonSocial != null ? razonSocial : nombre != null ? nombre : email;
    }
}
