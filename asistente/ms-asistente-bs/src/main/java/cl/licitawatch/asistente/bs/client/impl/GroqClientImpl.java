package cl.licitawatch.asistente.bs.client.impl;

import cl.licitawatch.asistente.bs.client.GroqClient;
import cl.licitawatch.common.exception.LicitaWatchException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Groq Chat Completions (API compatible con OpenAI). La API key solo viaja en la cabecera Authorization. */
@Slf4j
@Component
public class GroqClientImpl implements GroqClient {
    private final RestClient rest;
    private final String apiKey;
    private final String model;

    public GroqClientImpl(@Value("${licitawatch.groq.base-url}") String baseUrl, @Value("${licitawatch.groq.api-key:}") String apiKey,
                          @Value("${licitawatch.groq.model}") String model, @Value("${licitawatch.groq.timeout-seconds:60}") int timeout) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(5000);
        f.setReadTimeout(timeout * 1000);
        this.rest = RestClient.builder().baseUrl(baseUrl).requestFactory(f).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String modelo() {
        return model;
    }

    @Override
    public String completar(List<Mensaje> mensajes) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AsistenteException(HttpStatus.SERVICE_UNAVAILABLE, "ASISTENTE_NO_CONFIGURADO", "LicitAsist no está configurado (falta GROQ_API_KEY).");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", mensajes);
        body.put("temperature", 0.3);
        body.put("max_completion_tokens", 2048);
        body.put("reasoning_effort", "low");
        try {
            JsonNode r = rest.post().uri("/chat/completions").header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(JsonNode.class);
            String contenido = r == null ? null : r.path("choices").path(0).path("message").path("content").asText(null);
            if (contenido == null || contenido.isBlank()) {
                throw new AsistenteException(HttpStatus.BAD_GATEWAY, "ASISTENTE_SIN_RESPUESTA", "LicitAsist no generó una respuesta. Intenta reformular tu pregunta.");
            }
            return contenido.trim();
        } catch (HttpStatusCodeException e) {
            log.error("Groq respondió {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 429) {
                throw new AsistenteException(HttpStatus.TOO_MANY_REQUESTS, "ASISTENTE_SATURADO", "LicitAsist recibió demasiadas consultas. Espera unos segundos.");
            }
            throw new AsistenteException(HttpStatus.BAD_GATEWAY, "ASISTENTE_ERROR", "LicitAsist no está disponible en este momento.");
        } catch (RestClientException e) {
            log.error("Groq no disponible: {}", e.getMessage());
            throw new AsistenteException(HttpStatus.SERVICE_UNAVAILABLE, "ASISTENTE_NO_DISPONIBLE", "LicitAsist no está disponible en este momento.");
        }
    }

    public static class AsistenteException extends LicitaWatchException {
        public AsistenteException(HttpStatus status, String codigo, String mensaje) {
            super(status, codigo, mensaje);
        }
    }
}
