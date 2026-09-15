package cl.licitawatch.asistente.groq;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Cuerpo de POST {base-url}/chat/completions - API compatible con OpenAI. */
public record GroqChatRequest(
        String model,
        List<GroqMensaje> messages,
        double temperature,
        @JsonProperty("max_tokens") int maxTokens
) {
}
