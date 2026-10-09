package cl.licitawatch.ventas.bs.client;

import cl.licitawatch.ventas.bs.client.dto.CrearTransaccionDto;
import cl.licitawatch.ventas.bs.client.dto.ResultadoPagoDto;
import cl.licitawatch.ventas.bs.client.dto.TransaccionDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

/** Contrato REST de MS.ventas.ambassador (MS.ventas.bs nunca habla con Transbank directamente). */
@HttpExchange("/pasarela/transacciones")
public interface PasarelaClient {
    @PostExchange
    TransaccionDto crear(@RequestBody CrearTransaccionDto request);

    @PutExchange("/{token}")
    ResultadoPagoDto confirmar(@PathVariable String token);

    @GetExchange("/{token}")
    ResultadoPagoDto estado(@PathVariable String token);
}
