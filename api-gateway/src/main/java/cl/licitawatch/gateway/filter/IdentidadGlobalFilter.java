package cl.licitawatch.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Reenvía a los BFF la identidad del JWT validado (X-User-*) y la clave interna.
 * Siempre elimina las cabeceras X-User-* / X-Internal-Key que vengan del cliente (no se pueden falsificar).
 */
@Component
public class IdentidadGlobalFilter implements GlobalFilter, Ordered {
    private final String apiKey;

    public IdentidadGlobalFilter(@Value("${licitawatch.internal.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .flatMap(principal -> {
                    ServerHttpRequest.Builder b = exchange.getRequest().mutate().headers(h -> {
                        h.remove("X-User-Id");
                        h.remove("X-User-Email");
                        h.remove("X-User-Rol");
                        h.remove("X-Perfil-Id");
                        h.remove("X-Internal-Key");
                        h.set("X-Internal-Key", apiKey);
                        if (principal.isPresent() && principal.get() instanceof JwtAuthenticationToken jwt) {
                            h.set("X-User-Id", jwt.getToken().getSubject());
                            Optional.ofNullable(jwt.getToken().getClaimAsString("email")).ifPresent(v -> h.set("X-User-Email", v));
                            Optional.ofNullable(jwt.getToken().getClaimAsString("rol")).ifPresent(v -> h.set("X-User-Rol", v));
                            Object perfil = jwt.getToken().getClaims().get("perfilId");
                            if (perfil != null) {
                                h.set("X-Perfil-Id", String.valueOf(perfil));
                            }
                        }
                    });
                    return chain.filter(exchange.mutate().request(b.build()).build());
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
