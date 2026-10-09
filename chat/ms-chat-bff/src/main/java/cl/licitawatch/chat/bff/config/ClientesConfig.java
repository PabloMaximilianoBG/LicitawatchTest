package cl.licitawatch.chat.bff.config;

import cl.licitawatch.chat.bff.client.ChatBsClient;
import cl.licitawatch.common.cliente.RestClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientesConfig {
    @Bean
    public ChatBsClient chatBsClient(RestClientFactory f, @Value("${licitawatch.servicios.chat-bs}") String url) {
        return f.crear(ChatBsClient.class, url);
    }
}
