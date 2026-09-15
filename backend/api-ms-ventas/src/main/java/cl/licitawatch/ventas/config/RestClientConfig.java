package cl.licitawatch.ventas.config;

import java.time.Duration;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /**
     * Usado por NotificacionClient. Ver la misma nota en api-licitaciones:
     * Notificaciones hace un envio real de correo por SMTP antes de
     * responder (3-4s contra Gmail en la practica), asi que el timeout de
     * lectura es mas generoso (10s) que el de las llamadas internas
     * puramente de base de datos.
     */
    @Bean
    public RestClient restClient() {
        var settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(2))
                .withReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder()
                .requestFactory(ClientHttpRequestFactories.get(settings))
                .build();
    }
}
