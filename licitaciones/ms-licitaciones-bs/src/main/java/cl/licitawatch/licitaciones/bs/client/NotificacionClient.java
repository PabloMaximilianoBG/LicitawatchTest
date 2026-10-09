package cl.licitawatch.licitaciones.bs.client;

import cl.licitawatch.licitaciones.bs.client.dto.NotificacionDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/** Contrato REST de MS.notificaciones.bs (correo + registro en notificacion). */
@HttpExchange("/bs/notificaciones")
public interface NotificacionClient {
    @PostExchange
    void notificar(@RequestBody NotificacionDto request);
}
