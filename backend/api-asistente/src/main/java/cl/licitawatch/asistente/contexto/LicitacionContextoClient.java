package cl.licitawatch.asistente.contexto;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * A diferencia de UsuarioContextoClient/VentaContextoClient (que llaman
 * endpoints /internal/** sin autenticacion), estas llamadas SI requieren el
 * JWT del usuario: se reenvia tal cual el header Authorization que llego al
 * chat, para que Licitaciones aplique exactamente la misma autorizacion que
 * si el usuario hubiera llamado a su propio endpoint REST directamente
 * (seccion 3.5: "el usuario solo debe poder pedir informacion a la que su
 * rol/JWT le da acceso").
 */
@Component
public class LicitacionContextoClient {

    private static final Logger log = LoggerFactory.getLogger(LicitacionContextoClient.class);

    private final RestClient restClient;
    private final String baseUrl;

    public LicitacionContextoClient(@Qualifier("internoRestClient") RestClient restClient,
                                     @Value("${licitawatch.licitaciones.base-url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    public List<LicitacionResponse> misLicitaciones(String authorizationHeader) {
        return llamarLista(authorizationHeader, "/api/licitaciones/mias", "mis licitaciones",
                new ParameterizedTypeReference<List<LicitacionResponse>>() {
                });
    }

    public List<PostulacionResponse> misPostulaciones(String authorizationHeader) {
        return llamarLista(authorizationHeader, "/api/postulaciones/mias", "mis postulaciones",
                new ParameterizedTypeReference<List<PostulacionResponse>>() {
                });
    }

    public List<LicitacionResponse> licitacionesPublicadas(String authorizationHeader) {
        return llamarLista(authorizationHeader, "/api/licitaciones", "licitaciones publicadas",
                new ParameterizedTypeReference<List<LicitacionResponse>>() {
                });
    }

    private <T> List<T> llamarLista(String authorizationHeader, String path, String descripcion, ParameterizedTypeReference<List<T>> tipo) {
        try {
            return restClient.get()
                    .uri(baseUrl + path)
                    .header("Authorization", authorizationHeader)
                    .retrieve()
                    .body(tipo);
        } catch (Exception ex) {
            log.warn("No se pudo obtener '{}' para el contexto de LicitAsist: {}", descripcion, ex.getMessage());
            return List.of();
        }
    }
}
