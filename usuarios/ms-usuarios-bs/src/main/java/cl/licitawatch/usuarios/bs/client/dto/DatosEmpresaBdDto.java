package cl.licitawatch.usuarios.bs.client.dto;

import lombok.Builder;

@Builder
public record DatosEmpresaBdDto(String razonSocial, String rut, String nombreContacto, String emailContacto, String telefono,
                                Integer rubroId, Integer ciudadId, Integer tamanoEmpresaId, String descripcionEmpresa,
                                String sitioWeb) {
}
