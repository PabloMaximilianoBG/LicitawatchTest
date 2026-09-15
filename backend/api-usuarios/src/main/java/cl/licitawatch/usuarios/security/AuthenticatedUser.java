package cl.licitawatch.usuarios.security;

public record AuthenticatedUser(
        Long id,
        String email,
        String rol
) {
}
