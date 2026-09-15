package cl.licitawatch.asistente.contexto;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UsuarioContextoClient {

    private static final Logger log = LoggerFactory.getLogger(UsuarioContextoClient.class);

    private final RestClient restClient;
    private final String baseUrl;

    public UsuarioContextoClient(@Qualifier("internoRestClient") RestClient restClient,
                                  @Value("${licitawatch.usuarios.base-url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    public Optional<UsuarioInternoResponse> resolver(Long usuarioId) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri(baseUrl + "/internal/usuarios/{id}", usuarioId)
                    .retrieve()
                    .body(UsuarioInternoResponse.class));
        } catch (Exception ex) {
            log.warn("No se pudo resolver el usuario {} para el contexto de LicitAsist: {}", usuarioId, ex.getMessage());
            return Optional.empty();
        }
    }
}
