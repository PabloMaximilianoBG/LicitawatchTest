package cl.licitawatch.chat.bs.config;

import cl.licitawatch.chat.bs.client.ChatBdClient;
import cl.licitawatch.chat.bs.client.LicitacionClient;
import cl.licitawatch.chat.bs.client.NotificacionClient;
import cl.licitawatch.chat.bs.client.UsuarioClient;
import cl.licitawatch.common.cliente.RestClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientesConfig {
    @Bean
    public ChatBdClient chatBdClient(RestClientFactory f, @Value("${licitawatch.servicios.chat-bd}") String url) {
        return f.crear(ChatBdClient.class, url);
    }

    @Bean
    public LicitacionClient licitacionClient(RestClientFactory f, @Value("${licitawatch.servicios.licitaciones-bs}") String url) {
        return f.crear(LicitacionClient.class, url, 10);
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
