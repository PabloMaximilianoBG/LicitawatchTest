package cl.licitawatch.notificaciones.dto;

import cl.licitawatch.notificaciones.entity.CanalNotificacion;
import cl.licitawatch.notificaciones.entity.TipoNotificacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * "asunto"/"mensaje" solo se usan para componer el correo; no se persisten
 * en la tabla notificacion (esa solo guarda id, usuario_id, tipo, canal,
 * estado, fecha - seccion 3.4).
 */
public record NotificacionIngestaRequest(
        @NotNull Long usuarioId,
        @NotNull TipoNotificacion tipo,
        @NotNull CanalNotificacion canal,
        @NotBlank @Size(max = 150) String asunto,
        @NotBlank @Size(max = 2000) String mensaje
) {
}
