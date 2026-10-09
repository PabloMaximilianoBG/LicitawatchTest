package cl.licitawatch.chat.bs.client;

import cl.licitawatch.chat.bs.client.dto.NotificacionDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/bs/notificaciones")
public interface NotificacionClient {
    @PostExchange
    void notificar(@RequestBody NotificacionDto request);
}
