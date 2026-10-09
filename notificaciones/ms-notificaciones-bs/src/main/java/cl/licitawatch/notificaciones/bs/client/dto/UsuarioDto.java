package cl.licitawatch.notificaciones.bs.client.dto;

public record UsuarioDto(Integer usuarioId, String email, String rol, Boolean activo, String nombre, Integer perfilId) {
}
