package cl.licitawatch.asistente.contexto;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class VentaContextoClient {

    private static final Logger log = LoggerFactory.getLogger(VentaContextoClient.class);

    private final RestClient restClient;
    private final String baseUrl;

    public VentaContextoClient(@Qualifier("internoRestClient") RestClient restClient,
                                @Value("${licitawatch.ventas.base-url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    public Optional<SuscripcionResponse> obtenerSuscripcion(Long usuarioId) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri(baseUrl + "/internal/suscripciones/{id}", usuarioId)
                    .retrieve()
                    .body(SuscripcionResponse.class));
        } catch (Exception ex) {
            log.warn("No se pudo obtener la suscripcion del usuario {} para el contexto de LicitAsist: {}", usuarioId, ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * GET /api/planes exige JWT (a diferencia de los endpoints /internal/**
     * de este mismo servicio), asi que aqui si se reenvia el Authorization
     * del usuario - necesario para que LicitAsist pueda responder preguntas
     * como "compara los planes" (capacidad explicita de la seccion 3.5).
     */
    public List<PlanCatalogoResponse> obtenerCatalogoPlanes(String authorizationHeader) {
        try {
            return restClient.get()
                    .uri(baseUrl + "/api/planes")
                    .header("Authorization", authorizationHeader)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PlanCatalogoResponse>>() {
                    });
        } catch (Exception ex) {
            log.warn("No se pudo obtener el catalogo de planes para el contexto de LicitAsist: {}", ex.getMessage());
            return List.of();
        }
    }
}
