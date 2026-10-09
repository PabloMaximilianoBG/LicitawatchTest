package cl.licitawatch.asistente.bff.client;

import cl.licitawatch.asistente.bff.dto.request.ChatRequest;
import cl.licitawatch.asistente.bff.dto.response.ChatResponse;
import cl.licitawatch.asistente.bff.dto.response.EstadoAsistenteResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/bs/asistente")
public interface AsistenteBsClient {
    @GetExchange("/estado")
    EstadoAsistenteResponse estado();

    @PostExchange("/chat")
    ChatResponse chat(@RequestBody ChatRequest request);
}
