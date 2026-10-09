package cl.licitawatch.gateway.filter;

import cl.licitawatch.gateway.ratelimit.RateLimiterService;
import cl.licitawatch.gateway.security.RespuestasJson;
import cl.licitawatch.gateway.security.SqlInjectionGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Map;

/** Antes de Spring Security: bloquea patrones de SQL injection (400) y aplica rate limit (429 con Retry-After). */
@Component
@Order(-200)
@RequiredArgsConstructor
public class PerimetroWebFilter implements WebFilter {
    private final RateLimiterService rateLimiter;
    private final RespuestasJson respuestas;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        if (req.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }
        String path = req.getURI().getRawPath();
        if (SqlInjectionGuard.sospechoso(path) || SqlInjectionGuard.sospechoso(req.getURI().getRawQuery())) {
            return respuestas.escribir(exchange, HttpStatus.BAD_REQUEST, "SOLICITUD_RECHAZADA",
                    "La solicitud contiene caracteres o patrones no permitidos", null);
        }
        RateLimiterService.Categoria categoria = path.startsWith("/api/auth/") ? RateLimiterService.Categoria.AUTH
                : path.startsWith("/api/asistente/chat") ? RateLimiterService.Categoria.ASISTENTE : RateLimiterService.Categoria.GENERAL;
        RateLimiterService.Resultado r = rateLimiter.consumir(categoria, cliente(req));
        if (!r.permitido()) {
            return respuestas.escribir(exchange, HttpStatus.TOO_MANY_REQUESTS, "DEMASIADAS_SOLICITUDES",
                    "Demasiadas solicitudes. Espera " + r.esperarSegundos() + " segundos e intenta nuevamente.",
                    Map.of("Retry-After", String.valueOf(r.esperarSegundos())));
        }
        return chain.filter(exchange);
    }

    private static String cliente(ServerHttpRequest req) {
        InetSocketAddress remoto = req.getRemoteAddress();
        return remoto != null && remoto.getAddress() != null ? remoto.getAddress().getHostAddress() : "desconocido";
    }
}
