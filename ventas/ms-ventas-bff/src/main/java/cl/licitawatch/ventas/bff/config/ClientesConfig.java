package cl.licitawatch.ventas.bff.config;

import cl.licitawatch.common.cliente.RestClientFactory;
import cl.licitawatch.ventas.bff.client.VentasBsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientesConfig {
    @Bean
    public VentasBsClient ventasBsClient(RestClientFactory f, @Value("${licitawatch.servicios.ventas-bs}") String url) {
        return f.crear(VentasBsClient.class, url, 90);
    }
}
