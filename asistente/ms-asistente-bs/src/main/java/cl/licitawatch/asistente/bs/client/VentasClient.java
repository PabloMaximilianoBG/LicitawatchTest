package cl.licitawatch.asistente.bs.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/bs")
public interface VentasClient {
    @GetExchange("/suscripciones/plan-vigente/{usuarioId}")
    JsonNode planVigente(@PathVariable Integer usuarioId);

    @GetExchange("/admin/ventas/resumen")
    JsonNode resumen();

    @GetExchange("/admin/ventas")
    JsonNode adminVentas(@RequestParam int page, @RequestParam int size);

    @GetExchange("/admin/suscripciones")
    JsonNode adminSuscripciones(@RequestParam int page, @RequestParam int size);
}
