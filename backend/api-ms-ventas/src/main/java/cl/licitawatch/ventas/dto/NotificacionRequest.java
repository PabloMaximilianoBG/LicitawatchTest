package cl.licitawatch.ventas.dto;

public record NotificacionRequest(
        Long usuarioId,
        String tipo,
        String canal,
        String asunto,
        String mensaje
) {
}
