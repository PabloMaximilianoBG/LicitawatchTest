package cl.licitawatch.notificaciones.bs.config;

import cl.licitawatch.common.cliente.RestClientFactory;
import cl.licitawatch.notificaciones.bs.client.NotificacionBdClient;
import cl.licitawatch.notificaciones.bs.client.UsuarioClient;
import cl.licitawatch.notificaciones.bs.client.VentasClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientesConfig {
    @Bean
    public NotificacionBdClient notificacionBdClient(RestClientFactory f, @Value("${licitawatch.servicios.notificaciones-bd}") String url) {
        return f.crear(NotificacionBdClient.class, url);
    }

    @Bean
    public UsuarioClient usuarioClient(RestClientFactory f, @Value("${licitawatch.servicios.usuarios-bs}") String url) {
        return f.crear(UsuarioClient.class, url, 10);
    }

    @Bean
    public VentasClient ventasClient(RestClientFactory f, @Value("${licitawatch.servicios.ventas-bs}") String url) {
        return f.crear(VentasClient.class, url, 10);
    }
}
