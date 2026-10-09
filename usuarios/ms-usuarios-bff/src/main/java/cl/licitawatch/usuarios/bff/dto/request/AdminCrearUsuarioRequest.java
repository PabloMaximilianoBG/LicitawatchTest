package cl.licitawatch.usuarios.bff.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

/** El Administrador crea cuentas de cualquier rol (quedan activas). Datos de empresa para Licitador/Pyme; nombre/área para Administrador. */
@Builder
public record AdminCrearUsuarioRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password,
        @NotBlank(message = "Confirma la contraseña") String confirmPassword,
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
