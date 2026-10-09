package cl.licitawatch.notificaciones.bff.client;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.notificaciones.bff.dto.request.SoporteRequest;
import cl.licitawatch.notificaciones.bff.dto.response.NotificacionResponse;
import cl.licitawatch.notificaciones.bff.dto.response.SoporteResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/bs")
public interface NotificacionBsClient {
    @GetExchange("/notificaciones/mias")
    PaginaResponse<NotificacionResponse> mias(@RequestParam int page, @RequestParam int size);

    @PostExchange("/soporte")
    SoporteResponse soporte(@RequestBody SoporteRequest request);

    @GetExchange("/admin/notificaciones")
    PaginaResponse<NotificacionResponse> admin(@RequestParam(required = false) String tipo, @RequestParam int page, @RequestParam int size);
}
