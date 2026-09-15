package cl.licitawatch.usuarios.dto;

public record UsuarioInternoResponse(
        Long id,
        String email,
        String rol,
        String nombre,
        boolean activo
) {
}
