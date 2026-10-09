package cl.licitawatch.notificaciones.bff.config;

import cl.licitawatch.common.cliente.RestClientFactory;
import cl.licitawatch.notificaciones.bff.client.NotificacionBsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientesConfig {
    @Bean
    public NotificacionBsClient notificacionBsClient(RestClientFactory f, @Value("${licitawatch.servicios.notificaciones-bs}") String url) {
        return f.crear(NotificacionBsClient.class, url, 120);
    }
}
