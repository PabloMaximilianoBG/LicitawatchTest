package cl.licitawatch.licitaciones.dto;

/**
 * Contrato REST hacia POST /notificaciones en API Notificaciones (seccion 3.4
 * del diseno). "asunto"/"mensaje" solo se usan para componer el correo en el
 * momento del envio: no forman parte del modelo persistido de Notificaciones
 * (esa tabla solo guarda id, usuario_id, tipo, canal, estado, fecha).
 */
public record NotificacionRequest(
        Long usuarioId,
        String tipo,
        String canal,
        String asunto,
        String mensaje
) {
}
