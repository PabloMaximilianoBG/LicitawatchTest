package cl.licitawatch.asistente.bs.config;

import cl.licitawatch.asistente.bs.client.LicitacionesClient;
import cl.licitawatch.asistente.bs.client.UsuariosClient;
import cl.licitawatch.asistente.bs.client.VentasClient;
import cl.licitawatch.common.cliente.RestClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** El asistente consulta las otras APIs reenviando al usuario: cada una aplica su RBAC y su control de propiedad. */
@Configuration
public class ClientesConfig {
    @Bean
    public UsuariosClient usuariosClient(RestClientFactory f, @Value("${licitawatch.servicios.usuarios-bs}") String url) {
        return f.crear(UsuariosClient.class, url, 15);
    }

    @Bean
    public LicitacionesClient licitacionesClient(RestClientFactory f, @Value("${licitawatch.servicios.licitaciones-bs}") String url) {
        return f.crear(LicitacionesClient.class, url, 15);
    }

    @Bean
    public VentasClient ventasClient(RestClientFactory f, @Value("${licitawatch.servicios.ventas-bs}") String url) {
        return f.crear(VentasClient.class, url, 15);
    }
}
