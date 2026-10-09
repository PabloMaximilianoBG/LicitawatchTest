package cl.licitawatch.licitaciones.bs.client;

import cl.licitawatch.licitaciones.bs.client.dto.PlanVigenteDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/** Contrato REST de MS.ventas.bs: plan vigente de la Pyme (límite mensual de postulaciones). */
@HttpExchange("/bs/suscripciones")
public interface VentasClient {
    @GetExchange("/plan-vigente/{usuarioId}")
    PlanVigenteDto planVigente(@PathVariable Integer usuarioId);
}
