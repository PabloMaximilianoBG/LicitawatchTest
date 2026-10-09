package cl.licitawatch.asistente.bs.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/bs")
public interface LicitacionesClient {
    @GetExchange("/licitaciones")
    JsonNode buscar(@RequestParam String orden, @RequestParam int page, @RequestParam int size);

    @GetExchange("/licitaciones/mias")
    JsonNode mias();

    @GetExchange("/licitaciones/{id}")
    JsonNode licitacion(@PathVariable Integer id);

    @GetExchange("/licitaciones/{id}/postulaciones")
    JsonNode postulantes(@PathVariable Integer id);

    @GetExchange("/postulaciones/mias")
    JsonNode misPostulaciones();

    @GetExchange("/postulaciones/uso")
    JsonNode uso();

    @GetExchange("/admin/licitaciones")
    JsonNode adminLicitaciones(@RequestParam int page, @RequestParam int size);

    @GetExchange("/admin/postulaciones")
    JsonNode adminPostulaciones(@RequestParam int page, @RequestParam int size);
}
