package cl.licitawatch.notificaciones.dto;

/**
 * Espejo del contrato que expone GET /internal/usuarios/{id} en API
 * Usuarios. Notificaciones lo necesita para saber a que correo enviar: su
 * propia base de datos solo guarda usuario_id, nunca el email.
 */
public record UsuarioInternoResponse(
        Long id,
        String email,
        String rol,
        String nombre,
        boolean activo
) {
}
