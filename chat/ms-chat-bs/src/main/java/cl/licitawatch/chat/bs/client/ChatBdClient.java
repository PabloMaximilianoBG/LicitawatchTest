package cl.licitawatch.chat.bs.client;

import cl.licitawatch.chat.bs.client.dto.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

import java.util.List;

@HttpExchange("/bd/conversaciones")
public interface ChatBdClient {
    @PostExchange
    ConversacionBdDto crear(@RequestBody ConversacionBdRequestDto request);

    @GetExchange
    List<ConversacionBdDto> listar(@RequestParam(required = false) Integer licitadorId, @RequestParam(required = false) Integer pymeId,
                                   @RequestParam(required = false) Integer lectorId);

    @DeleteExchange
    ConteoDto eliminar(@RequestParam List<Integer> postulacionIds);

    @GetExchange("/{id}")
    ConversacionBdDto obtener(@PathVariable Integer id, @RequestParam(required = false) Integer lectorId);

    @GetExchange("/postulacion/{postulacionId}")
    ConversacionBdDto porPostulacion(@PathVariable Integer postulacionId, @RequestParam(required = false) Integer lectorId);

    @GetExchange("/{id}/mensajes")
    List<MensajeBdDto> mensajes(@PathVariable Integer id, @RequestParam(required = false) Integer despuesDeId);

    @PostExchange("/{id}/mensajes")
    MensajeBdDto enviar(@PathVariable Integer id, @RequestBody MensajeBdRequestDto request);

    @PatchExchange("/{id}/leidos")
    ConteoDto marcarLeidos(@PathVariable Integer id, @RequestParam Integer lectorId);

    @GetExchange("/{id}/no-leidos")
    ConteoDto noLeidos(@PathVariable Integer id, @RequestParam Integer lectorId);
}
