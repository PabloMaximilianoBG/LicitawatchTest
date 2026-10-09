package cl.licitawatch.usuarios.bs.config;

import cl.licitawatch.common.cliente.RestClientFactory;
import cl.licitawatch.usuarios.bs.client.NotificacionClient;
import cl.licitawatch.usuarios.bs.client.UsuarioBdClient;
import cl.licitawatch.usuarios.bs.client.VentasClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Clientes REST (interfaces @HttpExchange) hacia MS.usuarios.bd, MS.ventas.bs y MS.notificaciones.bs. */
@Configuration
public class ClientesConfig {

    @Bean
    public UsuarioBdClient usuarioBdClient(RestClientFactory f, @Value("${licitawatch.servicios.usuarios-bd}") String url) {
        return f.crear(UsuarioBdClient.class, url);
    }

    @Bean
    public VentasClient ventasClient(RestClientFactory f, @Value("${licitawatch.servicios.ventas-bs}") String url) {
        return f.crear(VentasClient.class, url, 5);
    }

    @Bean
    public NotificacionClient notificacionClient(RestClientFactory f, @Value("${licitawatch.servicios.notificaciones-bs}") String url) {
        return f.crear(NotificacionClient.class, url, 90);
    }
}
