package cl.licitawatch.asistente.security;

public record AuthenticatedUser(
        Long id,
        String email,
        String rol
) {
}
