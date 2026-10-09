package cl.licitawatch.asistente.bs.client;

import java.util.List;

/** Servicio externo servicio-groq (modelo de lenguaje). */
public interface GroqClient {
    record Mensaje(String role, String content) {
    }

    String completar(List<Mensaje> mensajes);

    String modelo();
}
