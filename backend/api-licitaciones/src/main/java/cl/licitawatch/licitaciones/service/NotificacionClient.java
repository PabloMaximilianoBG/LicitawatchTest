package cl.licitawatch.licitaciones.service;

import cl.licitawatch.licitaciones.dto.NotificacionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Llama por REST directa (sincrona, sin bus de mensajeria) a API
 * Notificaciones cuando ocurre un evento relevante (licitacion publicada,
 * postulacion recibida) - seccion 2 y 3.2 del diseno.
 *
 * Es best-effort: si Notificaciones no responde (por ejemplo, todavia no se
 * ha levantado, o esta caida), se registra un warning y el flujo principal
 * (publicar licitacion / postular) continua sin verse afectado. El mismo
 * principio de "un correo que falla no debe tumbar la operacion principal"
 * que aplica dentro de Notificaciones (seccion 3.4) aplica aqui un nivel
 * arriba, en quien la llama.
 */
@Component
public class NotificacionClient {

    private static final Logger log = LoggerFactory.getLogger(NotificacionClient.class);

    private final RestClient restClient;
    private final String baseUrl;

    public NotificacionClient(RestClient restClient, @Value("${licitawatch.notificaciones.base-url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    public void notificar(NotificacionRequest request) {
        try {
            restClient.post()
                    .uri(baseUrl + "/notificaciones")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.warn("No se pudo notificar a usuario {} (tipo={}): {}", request.usuarioId(), request.tipo(), ex.getMessage());
        }
    }
}
