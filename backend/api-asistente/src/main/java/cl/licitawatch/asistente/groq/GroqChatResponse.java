package cl.licitawatch.asistente.groq;

import java.util.List;

public record GroqChatResponse(
        List<GroqChoice> choices
) {

    public record GroqChoice(
            GroqMensaje message
    ) {
    }
}
