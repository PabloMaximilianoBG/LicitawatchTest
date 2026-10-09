package cl.licitawatch.licitaciones.bs.client.dto;

import lombok.Builder;

@Builder
public record PerfilDto(Integer usuarioId, String email, String rol, Boolean activo, Integer perfilId, String razonSocial,
                        String rut, String nombreContacto, String emailContacto, String telefono, String rubroNombre,
                        String ciudadNombre, String regionNombre, String tamanoEmpresaNombre, String descripcionEmpresa,
                        String sitioWeb, boolean premium) {
}
