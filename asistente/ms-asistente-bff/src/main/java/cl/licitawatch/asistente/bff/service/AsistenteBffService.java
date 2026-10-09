package cl.licitawatch.asistente.bff.service;

import cl.licitawatch.asistente.bff.dto.request.ChatRequest;
import cl.licitawatch.asistente.bff.dto.response.ChatResponse;
import cl.licitawatch.asistente.bff.dto.response.EstadoAsistenteResponse;

public interface AsistenteBffService {
    EstadoAsistenteResponse estado();

    ChatResponse chat(ChatRequest request);
}
