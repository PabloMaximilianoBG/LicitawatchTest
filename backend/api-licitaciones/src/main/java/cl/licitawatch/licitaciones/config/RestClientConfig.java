package cl.licitawatch.licitaciones.config;

import java.time.Duration;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /**
     * Usado por NotificacionClient. El timeout de lectura es mas generoso
     * que un simple GET local (10s, no 3s): Notificaciones hace un envio
     * real de correo por SMTP antes de responder (conectar + STARTTLS +
     * autenticar + enviar toma en la practica 3-4s contra Gmail), y un
     * timeout demasiado ajustado generaba falsos "no se pudo notificar" en
     * el log aunque el correo se enviara bien igual.
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
