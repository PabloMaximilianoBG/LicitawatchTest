package cl.licitawatch.chat.bff.service;

import cl.licitawatch.chat.bff.dto.request.AbrirConversacionRequest;
import cl.licitawatch.chat.bff.dto.request.MensajeRequest;
import cl.licitawatch.chat.bff.dto.response.ConversacionResponse;
import cl.licitawatch.chat.bff.dto.response.MensajeResponse;

import java.util.List;

public interface ChatBffService {
    ConversacionResponse abrir(AbrirConversacionRequest request);

    List<ConversacionResponse> mias();

    ConversacionResponse porPostulacion(Integer postulacionId);

    ConversacionResponse obtener(Integer id);

    List<MensajeResponse> mensajes(Integer id, Integer despuesDe);

    MensajeResponse enviar(Integer id, MensajeRequest request);
}
