package cl.licitawatch.usuarios.bff.config;

import cl.licitawatch.common.cliente.RestClientFactory;
import cl.licitawatch.usuarios.bff.client.UsuarioBsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientesConfig {
    @Bean
    public UsuarioBsClient usuarioBsClient(RestClientFactory f, @Value("${licitawatch.servicios.usuarios-bs}") String url) {
        return f.crear(UsuarioBsClient.class, url);
    }
}
