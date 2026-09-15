package cl.licitawatch.asistente.groq;

import cl.licitawatch.asistente.exception.AsistenteNoDisponibleException;
import cl.licitawatch.asistente.exception.LimiteGroqException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Llama al Free Tier de Groq (seccion 3.5 del diseno). Nunca expone la
 * GROQ_API_KEY al frontend: esta clase corre solo en el backend. Maneja el
 * limite de uso (HTTP 429) de forma explicita, y hace un numero acotado de
 * reintentos ante errores transitorios antes de avisarle claramente al
 * usuario que el asistente no esta disponible - nunca falla en silencio ni
 * promete disponibilidad ilimitada.
 */
@Component
public class GroqClient {

    private static final Logger log = LoggerFactory.getLogger(GroqClient.class);
    private static final int REINTENTOS_MAXIMOS = 2;

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public GroqClient(
            @Qualifier("groqRestClient") RestClient restClient,
            @Value("${groq.api-key}") String apiKey,
            @Value("${groq.model}") String model,
            @Value("${groq.base-url}") String baseUrl) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
    }

    public String responder(List<GroqMensaje> mensajes) {
        GroqChatRequest request = new GroqChatRequest(model, mensajes, 0.4, 700);

        for (int intento = 1; intento <= REINTENTOS_MAXIMOS; intento++) {
            try {
                GroqChatResponse respuesta = restClient.post()
                        .uri(baseUrl + "/chat/completions")
                        .header("Authorization", "Bearer " + apiKey)
                        .body(request)
                        .retrieve()
                        .body(GroqChatResponse.class);

                if (respuesta == null || respuesta.choices() == null || respuesta.choices().isEmpty()) {
                    throw new AsistenteNoDisponibleException(new IllegalStateException("Respuesta vacia de Groq"));
                }
                return respuesta.choices().get(0).message().content();

            } catch (RestClientResponseException ex) {
                if (ex.getStatusCode() == HttpStatusCode.valueOf(429)) {
                    throw new LimiteGroqException();
                }
                log.warn("Intento {}/{} fallido al llamar a Groq: {}", intento, REINTENTOS_MAXIMOS, ex.getMessage());
                if (intento == REINTENTOS_MAXIMOS) {
                    throw new AsistenteNoDisponibleException(ex);
                }
            } catch (Exception ex) {
                log.warn("Intento {}/{} fallido al llamar a Groq: {}", intento, REINTENTOS_MAXIMOS, ex.getMessage());
                if (intento == REINTENTOS_MAXIMOS) {
                    throw new AsistenteNoDisponibleException(ex);
                }
            }
        }
        throw new AsistenteNoDisponibleException(new IllegalStateException("No se pudo obtener respuesta de Groq"));
    }
}
