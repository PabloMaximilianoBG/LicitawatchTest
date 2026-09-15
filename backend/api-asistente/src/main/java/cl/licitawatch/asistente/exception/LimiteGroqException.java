package cl.licitawatch.asistente.exception;

import org.springframework.http.HttpStatus;

/**
 * Se alcanzo el limite del Free Tier de Groq (HTTP 429). Nunca se debe
 * presentar como "sin servicio ilimitado" - seccion 3.5 del diseno.
 */
public class LimiteGroqException extends ApiException {
    public LimiteGroqException() {
        super(HttpStatus.TOO_MANY_REQUESTS,
                "LicitAsist alcanzo el limite de uso del Free Tier de Groq por ahora. Intenta nuevamente en unos minutos.");
    }
}
