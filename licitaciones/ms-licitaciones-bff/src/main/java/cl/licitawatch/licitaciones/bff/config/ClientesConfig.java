package cl.licitawatch.licitaciones.bff.config;

import cl.licitawatch.common.cliente.RestClientFactory;
import cl.licitawatch.licitaciones.bff.client.LicitacionBsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientesConfig {
    @Bean
    public LicitacionBsClient licitacionBsClient(RestClientFactory f, @Value("${licitawatch.servicios.licitaciones-bs}") String url) {
        return f.crear(LicitacionBsClient.class, url, 60);
    }
}
