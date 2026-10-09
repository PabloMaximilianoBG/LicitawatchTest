package cl.licitawatch.usuarios.bff.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

/** Cambio de rol. Si el usuario aún no tiene el perfil del nuevo rol, se envían sus datos (ej: nombre y área para dar Administrador). */
@Builder
public record CambiarRolRequest(
        @NotBlank String rol,
        @Size(max = 150) String nombre,
        @Size(max = 150) String area,
        @Size(max = 200) String razonSocial,
        String rut,
        @Size(max = 150) String nombreContacto,
        @Email @Size(max = 255) String emailContacto,
        @Pattern(regexp = "^\\+?[0-9 ]{8,15}$", message = "Teléfono inválido") String telefono,
        Integer rubroId,
        Integer ciudadId,
        Integer tamanoEmpresaId,
        @Size(max = 2000) String descripcionEmpresa,
        @Size(max = 255) String sitioWeb) {
}
