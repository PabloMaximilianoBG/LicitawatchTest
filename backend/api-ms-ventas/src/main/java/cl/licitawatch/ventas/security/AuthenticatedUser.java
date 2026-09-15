package cl.licitawatch.ventas.security;

public record AuthenticatedUser(
        Long id,
        String email,
        String rol
) {
}
