package cl.licitawatch.notificaciones.bs.client;

import cl.licitawatch.notificaciones.bs.client.dto.PlanVigenteDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/** Para el nivel de soporte: Premium = prioritario (PPT diap. 7). */
@HttpExchange("/bs/suscripciones")
public interface VentasClient {
    @GetExchange("/plan-vigente/{usuarioId}")
    PlanVigenteDto planVigente(@PathVariable Integer usuarioId);
}
