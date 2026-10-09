package cl.licitawatch.usuarios.bs.dto.response;

/** Datos mínimos de un usuario para otros microservicios (correo de notificaciones, nombres). */
public record UsuarioBasicoResponse(Integer usuarioId, String email, String rol, Boolean activo, String nombre, Integer perfilId) {
}
