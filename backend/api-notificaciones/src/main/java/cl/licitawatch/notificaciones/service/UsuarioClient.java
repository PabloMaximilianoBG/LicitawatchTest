package cl.licitawatch.notificaciones.service;

import cl.licitawatch.notificaciones.dto.UsuarioInternoResponse;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Resuelve usuario_id -> email llamando a GET /internal/usuarios/{id} en API
 * Usuarios. No esta dibujada esta flecha en el diagrama de arquitectura de
 * la seccion 2, pero es imprescindible: Notificaciones no guarda emails, y
 * sin uno no hay a donde mandar el correo.
 */
@Component
public class UsuarioClient {

    private static final Logger log = LoggerFactory.getLogger(UsuarioClient.class);

    private final RestClient restClient;
    private final String baseUrl;

    public UsuarioClient(RestClient restClient, @Value("${licitawatch.usuarios.base-url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    public Optional<UsuarioInternoResponse> resolver(Long usuarioId) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri(baseUrl + "/internal/usuarios/{id}", usuarioId)
                    .retrieve()
                    .body(UsuarioInternoResponse.class));
        } catch (Exception ex) {
            log.warn("No se pudo resolver el usuario {} para enviar la notificacion: {}", usuarioId, ex.getMessage());
            return Optional.empty();
        }
    }
}
