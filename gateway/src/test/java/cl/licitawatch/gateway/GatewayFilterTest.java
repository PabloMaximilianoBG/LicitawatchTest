package cl.licitawatch.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import cl.licitawatch.gateway.filter.GatewayAuthFilter;
import cl.licitawatch.gateway.ratelimit.AsistenteRateLimiter;
import cl.licitawatch.gateway.ratelimit.LoginRateLimiter;
import cl.licitawatch.gateway.security.JwtValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Prueba GatewayAuthFilter de forma aislada (sin levantar un servidor Netty
 * real): en el entorno donde corre este asistente, el arranque de cualquier
 * servidor embebido (Tomcat o Netty) falla por una limitacion del sandbox
 * al abrir el "wakeup pipe" interno de java.nio.channels.Selector (ver
 * MEMORIA.md, notas de entorno de las fases 1-5). Probar el filtro
 * directamente, con un ServerWebExchange simulado y una GatewayFilterChain
 * de prueba, evita ese problema por completo y de paso es la forma mas
 * rapida y estandar de testear un GlobalFilter individual.
 *
 * Lo que SI queda sin probar automaticamente aqui es que Spring Cloud
 * Gateway responda 404 para una ruta sin predicate definido (p. ej.
 * /internal/**): eso es comportamiento propio del framework
 * (RoutePredicateHandlerMapping), no codigo nuestro, y esta garantizado por
 * el simple hecho de no declarar una ruta hacia esos paths en
 * application.yml.
 */
class GatewayFilterTest {

    private static final String SECRET = "dGVzdC1zZWNyZXQtc29sby1wYXJhLXRlc3RzLW51bmNhLWVuLXByb2R1Y2Npb24tMTIzNDU2Nzg=";

    private GatewayAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new GatewayAuthFilter(
                new JwtValidator(SECRET),
                new LoginRateLimiter(3, 1),
                new AsistenteRateLimiter(3, 1),
                new ObjectMapper());
    }

    private String token(Long usuarioId, String rol) {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Date ahora = new Date();
        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                .claim("email", "u" + usuarioId + "@test.cl")
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + 3_600_000))
                .signWith(key)
                .compact();
    }

    private ServerWebExchange exchange(HttpMethod metodo, String path, String bearerTokenOrNull) {
        MockServerHttpRequest.BaseBuilder<?> builder = MockServerHttpRequest.method(metodo, path)
                .remoteAddress(new InetSocketAddress("192.168.1.50", 5555));
        if (bearerTokenOrNull != null) {
            builder.header("Authorization", "Bearer " + bearerTokenOrNull);
        }
        return MockServerWebExchange.from(builder.build());
    }

    private AtomicBoolean invocarFiltroYRegistrarSiSiguio(ServerWebExchange exchange) {
        AtomicBoolean siguio = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            siguio.set(true);
            return Mono.empty();
        };
        filter.filter(exchange, chain).block();
        return siguio;
    }

    @Test
    void rutaProtegidaSinTokenDevuelve401YNoContinuaLaCadena() {
        ServerWebExchange exchange = exchange(HttpMethod.GET, "/api/perfil", null);
        AtomicBoolean siguio = invocarFiltroYRegistrarSiSiguio(exchange);

        assertThat(siguio).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void rutaAdminSinRolAdministradorDevuelve403() {
        ServerWebExchange exchange = exchange(HttpMethod.GET, "/api/admin/usuarios", token(1L, "EMPRESA"));
        AtomicBoolean siguio = invocarFiltroYRegistrarSiSiguio(exchange);

        assertThat(siguio).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void rutaAdminConRolAdministradorContinuaLaCadena() {
        ServerWebExchange exchange = exchange(HttpMethod.GET, "/api/admin/usuarios", token(1L, "ADMINISTRADOR"));
        AtomicBoolean siguio = invocarFiltroYRegistrarSiSiguio(exchange);

        assertThat(siguio).isTrue();
    }

    @Test
    void licitacionesTodasEsSoloAdminAunquePerteneceAOtroServicio() {
        ServerWebExchange exchangeEmpresa = exchange(HttpMethod.GET, "/api/licitaciones/todas", token(2L, "EMPRESA"));
        assertThat(invocarFiltroYRegistrarSiSiguio(exchangeEmpresa)).isFalse();
        assertThat(exchangeEmpresa.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ServerWebExchange exchangeAdmin = exchange(HttpMethod.GET, "/api/licitaciones/todas", token(2L, "ADMINISTRADOR"));
        assertThat(invocarFiltroYRegistrarSiSiguio(exchangeAdmin)).isTrue();
    }

    @Test
    void loginEsPublicoPeroTieneRateLimitPorIp() {
        for (int i = 0; i < 3; i++) {
            ServerWebExchange exchange = exchange(HttpMethod.POST, "/api/auth/login", null);
            assertThat(invocarFiltroYRegistrarSiSiguio(exchange)).isTrue();
        }

        ServerWebExchange cuartoIntento = exchange(HttpMethod.POST, "/api/auth/login", null);
        assertThat(invocarFiltroYRegistrarSiSiguio(cuartoIntento)).isFalse();
        assertThat(cuartoIntento.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void asistenteTieneRateLimitPorUsuario() {
        String token = token(500L, "CLIENTE");
        for (int i = 0; i < 3; i++) {
            ServerWebExchange exchange = exchange(HttpMethod.POST, "/api/asistente/chat", token);
            assertThat(invocarFiltroYRegistrarSiSiguio(exchange)).isTrue();
        }

        ServerWebExchange cuartoIntento = exchange(HttpMethod.POST, "/api/asistente/chat", token);
        assertThat(invocarFiltroYRegistrarSiSiguio(cuartoIntento)).isFalse();
        assertThat(cuartoIntento.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void rutasDeAutenticacionPublicasNoRequierenToken() {
        for (String path : new String[]{"/api/auth/registro/empresa", "/api/auth/registro/cliente", "/api/auth/refresh", "/api/auth/logout"}) {
            ServerWebExchange exchange = exchange(HttpMethod.POST, path, null);
            assertThat(invocarFiltroYRegistrarSiSiguio(exchange)).isTrue();
        }
    }
}
