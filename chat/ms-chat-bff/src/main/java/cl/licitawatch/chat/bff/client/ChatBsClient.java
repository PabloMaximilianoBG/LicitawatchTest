package cl.licitawatch.chat.bff.client;

import cl.licitawatch.chat.bff.dto.request.AbrirConversacionRequest;
import cl.licitawatch.chat.bff.dto.request.MensajeRequest;
import cl.licitawatch.chat.bff.dto.response.ConversacionResponse;
import cl.licitawatch.chat.bff.dto.response.MensajeResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange("/bs/conversaciones")
public interface ChatBsClient {
    @PostExchange
    ConversacionResponse abrir(@RequestBody AbrirConversacionRequest request);

    @GetExchange
    List<ConversacionResponse> mias();

    @GetExchange("/postulacion/{postulacionId}")
    ConversacionResponse porPostulacion(@PathVariable Integer postulacionId);

    @GetExchange("/{id}")
    ConversacionResponse obtener(@PathVariable Integer id);

    @GetExchange("/{id}/mensajes")
    List<MensajeResponse> mensajes(@PathVariable Integer id, @RequestParam(required = false) Integer despuesDe);

    @PostExchange("/{id}/mensajes")
    MensajeResponse enviar(@PathVariable Integer id, @RequestBody MensajeRequest request);
}
