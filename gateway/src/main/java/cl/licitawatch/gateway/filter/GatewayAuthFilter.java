package cl.licitawatch.gateway.filter;

import cl.licitawatch.gateway.ratelimit.AsistenteRateLimiter;
import cl.licitawatch.gateway.ratelimit.LoginRateLimiter;
import cl.licitawatch.gateway.security.JwtValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Punto unico de autenticacion/autorizacion del sistema (seccion 4 del
 * diseno): valida el JWT localmente, aplica RBAC a nivel de ruta para las
 * secciones de administracion, y aplica rate limiting en login y en
 * LicitAsist. La autorizacion fina (dueño del recurso) se deja a cada
 * microservicio, tal como pide el diseno.
 *
 * Las rutas de registro/login/refresh/logout de API Usuarios son las UNICAS
 * que no requieren JWT (por definicion, todavia no existe uno). Todo lo
 * demas que llega hasta aca ya paso por una ruta definida en application.yml
 * - las rutas /internal/** de cada microservicio no tienen predicate
 * definido, asi que ni siquiera llegan a este filtro: el propio Gateway
 * responde 404 antes de intentar enrutar.
 */
@Component
public class GatewayAuthFilter implements GlobalFilter, Ordered {

    private static final Set<String> RUTAS_PUBLICAS = Set.of(
            "/api/auth/registro/empresa",
            "/api/auth/registro/cliente",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/logout"
    );

    private static final String RUTA_LOGIN = "/api/auth/login";
    private static final String PREFIJO_ADMIN_USUARIOS = "/api/admin/";
    private static final Set<String> RUTAS_SOLO_ADMIN = Set.of(
            "/api/licitaciones/todas",
            "/api/ventas/todas"
    );
    private static final String PREFIJO_ASISTENTE = "/api/asistente/";
    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    private final JwtValidator jwtValidator;
    private final LoginRateLimiter loginRateLimiter;
    private final AsistenteRateLimiter asistenteRateLimiter;
    private final ObjectMapper objectMapper;

    public GatewayAuthFilter(
            JwtValidator jwtValidator,
            LoginRateLimiter loginRateLimiter,
            AsistenteRateLimiter asistenteRateLimiter,
            ObjectMapper objectMapper) {
        this.jwtValidator = jwtValidator;
        this.loginRateLimiter = loginRateLimiter;
        this.asistenteRateLimiter = asistenteRateLimiter;
        this.objectMapper = objectMapper;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (RUTAS_PUBLICAS.contains(path)) {
            if (RUTA_LOGIN.equals(path) && !loginRateLimiter.permitir(direccionIp(request))) {
                return responderError(exchange, HttpStatus.TOO_MANY_REQUESTS,
                        "Demasiados intentos de inicio de sesion, intenta nuevamente en un minuto");
            }
            return chain.filter(exchange);
        }

        String header = request.getHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return responderError(exchange, HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        Optional<Claims> claimsOpt = jwtValidator.validar(header.substring(7));
        if (claimsOpt.isEmpty()) {
            return responderError(exchange, HttpStatus.UNAUTHORIZED, "Token invalido o expirado");
        }

        Claims claims = claimsOpt.get();
        String rol = claims.get("rol", String.class);

        boolean esRutaSoloAdmin = path.startsWith(PREFIJO_ADMIN_USUARIOS) || RUTAS_SOLO_ADMIN.contains(path);
        if (esRutaSoloAdmin && !ROL_ADMINISTRADOR.equals(rol)) {
            return responderError(exchange, HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta accion");
        }

        if (path.startsWith(PREFIJO_ASISTENTE)) {
            Long usuarioId = Long.valueOf(claims.getSubject());
            if (!asistenteRateLimiter.permitir(usuarioId)) {
                return responderError(exchange, HttpStatus.TOO_MANY_REQUESTS,
                        "Alcanzaste el limite de uso de LicitAsist por ahora, intenta nuevamente en unos minutos");
            }
        }

        return chain.filter(exchange);
    }

    private String direccionIp(ServerHttpRequest request) {
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        return remoteAddress != null && remoteAddress.getAddress() != null
                ? remoteAddress.getAddress().getHostAddress()
                : "desconocida";
    }

    private Mono<Void> responderError(ServerWebExchange exchange, HttpStatus status, String mensaje) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> cuerpo = Map.of(
                "status", status.value(),
                "mensaje", mensaje,
                "path", exchange.getRequest().getURI().getPath(),
                "timestamp", LocalDateTime.now().toString());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(cuerpo);
        } catch (Exception ex) {
            bytes = ("{\"mensaje\":\"" + mensaje + "\"}").getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
