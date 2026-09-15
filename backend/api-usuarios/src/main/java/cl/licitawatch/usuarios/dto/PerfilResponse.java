package cl.licitawatch.usuarios.dto;

public record PerfilResponse(
        Long id,
        String email,
        String rol,
        boolean activo,
        String razonSocial,
        String rubro,
        String nombreContacto,
        String rut,
        String area
) {
}
