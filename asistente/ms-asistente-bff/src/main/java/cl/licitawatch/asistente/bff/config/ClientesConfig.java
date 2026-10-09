package cl.licitawatch.asistente.bff.config;

import cl.licitawatch.asistente.bff.client.AsistenteBsClient;
import cl.licitawatch.common.cliente.RestClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientesConfig {
    @Bean
    public AsistenteBsClient asistenteBsClient(RestClientFactory f, @Value("${licitawatch.servicios.asistente-bs}") String url) {
        return f.crear(AsistenteBsClient.class, url, 120);
    }
}
