package cl.licitawatch.asistente.bs.service;

import cl.licitawatch.asistente.bs.dto.request.ChatRequest;
import cl.licitawatch.asistente.bs.dto.response.ChatResponse;
import cl.licitawatch.asistente.bs.dto.response.EstadoAsistenteResponse;
import cl.licitawatch.common.seguridad.UsuarioActual;

/** LicitAsist (PPT diap. 9): chatbot integrado que usa datos reales de la plataforma vía Groq API. */
public interface AsistenteService {
    EstadoAsistenteResponse estado(UsuarioActual usuario);

    ChatResponse chat(UsuarioActual usuario, ChatRequest request);
}
