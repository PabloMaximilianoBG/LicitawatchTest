package cl.licitawatch.ventas.service;

import cl.licitawatch.ventas.dto.NotificacionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Igual patron que en API Licitaciones: llamada REST directa best-effort a
 * API Notificaciones al confirmar un pago (seccion 3.3). Si Notificaciones
 * no responde, se loguea un warning y el flujo de pago no se ve afectado.
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
