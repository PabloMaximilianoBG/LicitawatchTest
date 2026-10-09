package cl.licitawatch.usuarios.bd.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/** Columnas de licitador / pyme. tamanoEmpresaId solo aplica a pyme. */
@Builder
public record DatosEmpresaBdRequest(String razonSocial, String rut, @NotBlank String nombreContacto,
                                    @NotBlank String emailContacto, @NotBlank String telefono,
                                    @NotNull Integer rubroId, @NotNull Integer ciudadId, Integer tamanoEmpresaId,
                                    @NotBlank String descripcionEmpresa, String sitioWeb) {
}
