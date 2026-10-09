package cl.licitawatch.asistente.bff.service.impl;

import cl.licitawatch.asistente.bff.client.AsistenteBsClient;
import cl.licitawatch.asistente.bff.dto.request.ChatRequest;
import cl.licitawatch.asistente.bff.dto.response.ChatResponse;
import cl.licitawatch.asistente.bff.dto.response.EstadoAsistenteResponse;
import cl.licitawatch.asistente.bff.service.AsistenteBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsistenteBffServiceImpl implements AsistenteBffService {
    private final AsistenteBsClient bs;

    @Override
    public EstadoAsistenteResponse estado() {
        return bs.estado();
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        return bs.chat(request);
    }
}
