package cl.licitawatch.chat.bff.service.impl;

import cl.licitawatch.chat.bff.client.ChatBsClient;
import cl.licitawatch.chat.bff.dto.request.AbrirConversacionRequest;
import cl.licitawatch.chat.bff.dto.request.MensajeRequest;
import cl.licitawatch.chat.bff.dto.response.ConversacionResponse;
import cl.licitawatch.chat.bff.dto.response.MensajeResponse;
import cl.licitawatch.chat.bff.service.ChatBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatBffServiceImpl implements ChatBffService {
    private final ChatBsClient bs;

    @Override
    public ConversacionResponse abrir(AbrirConversacionRequest request) {
        return bs.abrir(request);
    }

    @Override
    public List<ConversacionResponse> mias() {
        return bs.mias();
    }

    @Override
    public ConversacionResponse porPostulacion(Integer postulacionId) {
        return bs.porPostulacion(postulacionId);
    }

    @Override
    public ConversacionResponse obtener(Integer id) {
        return bs.obtener(id);
    }

    @Override
    public List<MensajeResponse> mensajes(Integer id, Integer despuesDe) {
        return bs.mensajes(id, despuesDe);
    }

    @Override
    public MensajeResponse enviar(Integer id, MensajeRequest request) {
        return bs.enviar(id, request);
    }
}
