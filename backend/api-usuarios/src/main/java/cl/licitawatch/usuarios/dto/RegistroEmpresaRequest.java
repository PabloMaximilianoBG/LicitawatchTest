package cl.licitawatch.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroEmpresaRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 72) String contrasena,
        @NotBlank @Size(max = 200) String razonSocial,
        @NotBlank @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "formato de RUT invalido, use 12345678-9") String rut,
        @Size(max = 100) String rubro
) {
}
