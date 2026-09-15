package cl.licitawatch.notificaciones.entity;

/**
 * Solo 2 estados por diseno (seccion 3.4). Un intento de envio fallido
 * (SMTP caido, limite de Gmail alcanzado, etc.) se representa como
 * PENDIENTE en vez de un tercer estado "fallido" - se loguea el motivo, ver
 * NotificacionService.
 */
public enum EstadoNotificacion {
    ENVIADO,
    PENDIENTE
}
