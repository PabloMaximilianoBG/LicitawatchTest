package cl.licitawatch.usuarios.bs.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Edición del perfil. Licitador/Pyme: datos de la empresa (el RUT y el correo de acceso NO son editables).
 * Administrador: nombre y área. Los obligatorios por rol se validan en el servicio.
 */
@Builder
public record ActualizarPerfilRequest(
        @Size(max = 200) String razonSocial,
        @Size(max = 150) String nombreContacto,
        @Email(message = "Correo de contacto inválido") @Size(max = 255) String emailContacto,
        @Pattern(regexp = "^\\+?[0-9 ]{8,15}$", message = "Teléfono inválido") String telefono,
        Integer rubroId,
        Integer ciudadId,
        Integer tamanoEmpresaId,
        @Size(max = 2000) String descripcionEmpresa,
        @Size(max = 255) String sitioWeb,
        @Size(max = 150) String nombre,
        @Size(max = 150) String area) {
}
