package cl.licitawatch.notificaciones.bs.client;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.notificaciones.bs.client.dto.CatalogoDto;
import cl.licitawatch.notificaciones.bs.client.dto.NotificacionBdDto;
import cl.licitawatch.notificaciones.bs.client.dto.NotificacionBdRequestDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange("/bd")
public interface NotificacionBdClient {
    @PostExchange("/notificaciones")
    NotificacionBdDto registrar(@RequestBody NotificacionBdRequestDto request);

    @GetExchange("/notificaciones")
    PaginaResponse<NotificacionBdDto> listar(@RequestParam(required = false) Integer usuarioId, @RequestParam(required = false) String tipo,
                                             @RequestParam int page, @RequestParam int size);

    @GetExchange("/tipos-notificacion")
    List<CatalogoDto> tipos();
}
