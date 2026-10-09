package cl.licitawatch.licitaciones.bs.config;

import cl.licitawatch.common.cliente.RestClientFactory;
import cl.licitawatch.licitaciones.bs.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientesConfig {

    @Bean
    public LicitacionBdClient licitacionBdClient(RestClientFactory f, @Value("${licitawatch.servicios.licitaciones-bd}") String url) {
        return f.crear(LicitacionBdClient.class, url);
    }

    @Bean
    public UsuarioClient usuarioClient(RestClientFactory f, @Value("${licitawatch.servicios.usuarios-bs}") String url) {
        return f.crear(UsuarioClient.class, url);
    }

    @Bean
    public VentasClient ventasClient(RestClientFactory f, @Value("${licitawatch.servicios.ventas-bs}") String url) {
        return f.crear(VentasClient.class, url, 10);
    }

    @Bean
    public NotificacionClient notificacionClient(RestClientFactory f, @Value("${licitawatch.servicios.notificaciones-bs}") String url) {
        return f.crear(NotificacionClient.class, url, 90);
    }

    @Bean
    public ChatClient chatClient(RestClientFactory f, @Value("${licitawatch.servicios.chat-bs}") String url) {
        return f.crear(ChatClient.class, url, 10);
    }
}
