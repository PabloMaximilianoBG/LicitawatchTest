package cl.licitawatch.gateway;

import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/** Gateway real contra un BFF simulado que devuelve las cabeceras recibidas. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewaySeguridadTest {
    static final String SECRET = "s".repeat(64);
    static final Map<String, String> RECIBIDAS = new ConcurrentHashMap<>();
    static HttpServer bff;

    @Autowired
    WebTestClient web;

    @BeforeAll
    static void iniciarBff() throws Exception {
        bff = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        bff.createContext("/", ex -> {
            RECIBIDAS.clear();
            ex.getRequestHeaders().forEach((k, v) -> RECIBIDAS.put(k.toLowerCase(), v.get(0)));
            byte[] body = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        bff.start();
    }

    @AfterAll
    static void detener() {
        bff.stop(0);
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        String url = "http://127.0.0.1:" + bff.getAddress().getPort();
        for (String k : new String[]{"USUARIOS_BFF_URL", "LICITACIONES_BFF_URL", "VENTAS_BFF_URL", "NOTIFICACIONES_BFF_URL",
                "ASISTENTE_BFF_URL", "CHAT_BFF_URL"}) {
            r.add(k, () -> url);
        }
        r.add("licitawatch.jwt.secret", () -> SECRET);
        r.add("licitawatch.internal.api-key", () -> "clave-interna");
        r.add("licitawatch.rate-limit.auth-per-minute", () -> "1000");
    }

    static String token(String rol, byte[] clave) {
        return Jwts.builder().issuer("licitawatch").subject("7").claim("rol", rol).claim("perfilId", 3).claim("email", "a@b.cl")
                .issuedAt(new Date()).expiration(Date.from(Instant.now().plusSeconds(600)))
                .signWith(Keys.hmacShaKeyFor(clave), Jwts.SIG.HS512).compact();
    }

    static String token(String rol) {
        return token(rol, SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void rutaPublicaSinToken() {
        web.post().uri("/api/auth/login").exchange().expectStatus().isOk();
        assertThat(RECIBIDAS.get("x-internal-key")).isEqualTo("clave-interna");
        assertThat(RECIBIDAS).doesNotContainKey("x-user-id");
    }

    @Test
    void rutaProtegidaSinToken401() {
        web.get().uri("/api/usuarios/me").exchange().expectStatus().isUnauthorized().expectBody().jsonPath("$.codigo").isEqualTo("NO_AUTENTICADO");
    }

    @Test
    void tokenValidoPropagaLaIdentidadYDescartaCabecerasFalsas() {
        web.get().uri("/api/usuarios/me").header("Authorization", "Bearer " + token("PYME"))
                .header("X-User-Id", "999").header("X-Internal-Key", "falsa").exchange().expectStatus().isOk();
        assertThat(RECIBIDAS.get("x-user-id")).isEqualTo("7");
        assertThat(RECIBIDAS.get("x-user-rol")).isEqualTo("PYME");
        assertThat(RECIBIDAS.get("x-perfil-id")).isEqualTo("3");
        assertThat(RECIBIDAS.get("x-internal-key")).isEqualTo("clave-interna");
    }

    @Test
    void rbac_pymeNoEntraAlPanelAdmin_yLicitadorNoCompraPlanes() {
        web.get().uri("/api/admin/usuarios").header("Authorization", "Bearer " + token("PYME")).exchange().expectStatus().isForbidden();
        web.post().uri("/api/ventas/premium").header("Authorization", "Bearer " + token("LICITADOR")).exchange().expectStatus().isForbidden();
        web.get().uri("/api/admin/usuarios").header("Authorization", "Bearer " + token("ADMINISTRADOR")).exchange().expectStatus().isOk();
    }

    @Test
    void tokenDeCorreoNoSirveComoSesion() throws Exception {
        byte[] derivada = MessageDigest.getInstance("SHA-512").digest((SECRET + ":restablecer-password").getBytes(StandardCharsets.UTF_8));
        web.get().uri("/api/usuarios/me").header("Authorization", "Bearer " + token("PYME", derivada))
                .exchange().expectStatus().isUnauthorized().expectBody().jsonPath("$.codigo").isEqualTo("TOKEN_INVALIDO");
    }

    @Test
    void sqlInjectionEnQuery400() {
        web.get().uri("/api/licitaciones?q=1' OR '1'='1").header("Authorization", "Bearer " + token("PYME"))
                .exchange().expectStatus().isBadRequest();
    }

    @Test
    void corsSoloParaElFrontend() {
        web.options().uri("/api/planes").header("Origin", "http://localhost:5173").header("Access-Control-Request-Method", "GET")
                .exchange().expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:5173");
        web.options().uri("/api/planes").header("Origin", "http://malicioso.com").header("Access-Control-Request-Method", "GET")
                .exchange().expectStatus().isForbidden();
    }
}
