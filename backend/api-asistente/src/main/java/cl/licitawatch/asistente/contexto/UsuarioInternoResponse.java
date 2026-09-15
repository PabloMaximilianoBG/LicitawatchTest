package cl.licitawatch.asistente.contexto;

public record UsuarioInternoResponse(
        Long id,
        String email,
        String rol,
        String nombre,
        boolean activo
) {
}
