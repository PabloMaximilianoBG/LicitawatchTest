package cl.licitawatch.usuarios.bff.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record RegistroLicitadorRequest(
        @NotBlank(message = "El correo es obligatorio") @Email(message = "Correo inválido") @Size(max = 255) String email,
        @NotBlank(message = "La contraseña es obligatoria") @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password,
        @NotBlank(message = "Confirma la contraseña") String confirmPassword,
        @NotBlank(message = "La razón social es obligatoria") @Size(max = 200) String razonSocial,
        @NotBlank(message = "El RUT es obligatorio") String rut,
        @NotBlank(message = "El nombre de contacto es obligatorio") @Size(max = 150) String nombreContacto,
        @NotBlank(message = "El correo de contacto es obligatorio") @Email(message = "Correo de contacto inválido") @Size(max = 255) String emailContacto,
        @NotBlank(message = "El teléfono es obligatorio") @Pattern(regexp = "^\\+?[0-9 ]{8,15}$", message = "Teléfono inválido") String telefono,
        @NotNull(message = "Selecciona un rubro") Integer rubroId,
        @NotNull(message = "Selecciona una ciudad") Integer ciudadId,
        @NotBlank(message = "La descripción de la empresa es obligatoria") @Size(max = 2000) String descripcionEmpresa,
        @Size(max = 255) String sitioWeb) {
}
