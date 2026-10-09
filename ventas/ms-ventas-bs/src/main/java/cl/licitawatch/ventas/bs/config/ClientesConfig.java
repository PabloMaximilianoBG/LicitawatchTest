package cl.licitawatch.ventas.bs.config;

import cl.licitawatch.common.cliente.RestClientFactory;
import cl.licitawatch.ventas.bs.client.NotificacionClient;
import cl.licitawatch.ventas.bs.client.PasarelaClient;
import cl.licitawatch.ventas.bs.client.UsuarioClient;
import cl.licitawatch.ventas.bs.client.VentasBdClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientesConfig {

    @Bean
    public VentasBdClient ventasBdClient(RestClientFactory f, @Value("${licitawatch.servicios.ventas-bd}") String url) {
        return f.crear(VentasBdClient.class, url);
    }

    @Bean
    public PasarelaClient pasarelaClient(RestClientFactory f, @Value("${licitawatch.servicios.ventas-ambassador}") String url) {
        return f.crear(PasarelaClient.class, url, 60);
    }

    @Bean
    public UsuarioClient usuarioClient(RestClientFactory f, @Value("${licitawatch.servicios.usuarios-bs}") String url) {
        return f.crear(UsuarioClient.class, url, 10);
    }

    @Bean
    public NotificacionClient notificacionClient(RestClientFactory f, @Value("${licitawatch.servicios.notificaciones-bs}") String url) {
        return f.crear(NotificacionClient.class, url, 90);
    }
}
