package cl.licitawatch.licitaciones.security;

public record AuthenticatedUser(
        Long id,
        String email,
        String rol
) {
}
