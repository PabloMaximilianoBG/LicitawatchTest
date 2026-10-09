package cl.licitawatch.usuarios.bs.client;

import cl.licitawatch.usuarios.bs.client.dto.CorreoEnlaceDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/** Contrato REST de MS.notificaciones.bs para los correos de cuenta (no son eventos de tipo_notificacion). */
@HttpExchange("/bs/correos")
public interface NotificacionClient {

    @PostExchange("/confirmacion-cuenta")
    void confirmacionCuenta(@RequestBody CorreoEnlaceDto request);

    @PostExchange("/restablecer-password")
    void restablecerPassword(@RequestBody CorreoEnlaceDto request);
}
