package cl.licitawatch.common.cliente;

import cl.licitawatch.common.error.ErrorResponse;
import cl.licitawatch.common.exception.RemoteServiceException;
import cl.licitawatch.common.seguridad.CabecerasInternas;
import cl.licitawatch.common.seguridad.ContextoUsuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Crea clientes REST declarados como interfaces (@HttpExchange) hacia otros microservicios.
 * Agrega la clave interna, propaga el usuario de la petición en curso y convierte las respuestas de error
 * en {@link RemoteServiceException} (que el GlobalExceptionHandler reenvía con el mismo código HTTP).
 */
@RequiredArgsConstructor
public class RestClientFactory {
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public <T> T crear(Class<T> tipo, String baseUrl) {
        return crear(tipo, baseUrl, 30);
    }

    public <T> T crear(Class<T> tipo, String baseUrl, int timeoutSegundos) {
        // java.net.http.HttpClient: soporta PATCH (HttpURLConnection no) y HTTP/1.1 con keep-alive.
        HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).version(HttpClient.Version.HTTP_1_1).build();
        JdkClientHttpRequestFactory http = new JdkClientHttpRequestFactory(cliente);
        http.setReadTimeout(Duration.ofSeconds(timeoutSegundos));
        RestClient rest = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(http)
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().set(CabecerasInternas.INTERNAL_KEY, apiKey);
                    ContextoUsuario.actual().ifPresent(u -> {
                        request.getHeaders().set(CabecerasInternas.USUARIO_ID, String.valueOf(u.usuarioId()));
                        request.getHeaders().set(CabecerasInternas.USUARIO_ROL, u.rol());
                        if (u.email() != null) {
                            request.getHeaders().set(CabecerasInternas.USUARIO_EMAIL, u.email());
                        }
                        if (u.perfilId() != null) {
                            request.getHeaders().set(CabecerasInternas.PERFIL_ID, String.valueOf(u.perfilId()));
                        }
                    });
                    return execution.execute(request, body);
                })
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    ErrorResponse error = null;
                    try {
                        byte[] cuerpo = response.getBody().readAllBytes();
                        if (cuerpo.length > 0) {
                            error = objectMapper.readValue(cuerpo, ErrorResponse.class);
                        }
                    } catch (Exception ignored) {
                        // cuerpo no JSON: se informa solo el código
                    }
                    throw new RemoteServiceException(response.getStatusCode().value(), error);
                })
                .build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(rest)).build().createClient(tipo);
    }
}
