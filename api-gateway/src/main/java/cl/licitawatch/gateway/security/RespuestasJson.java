package cl.licitawatch.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Respuestas de error del gateway con el mismo formato que los microservicios (sin stack traces). */
@Component
@RequiredArgsConstructor
public class RespuestasJson {
    private final ObjectMapper om;

    public Mono<Void> noAutenticado(ServerWebExchange exchange, AuthenticationException e) {
        String auth = exchange.getRequest().getHeaders().getFirst("Authorization");
        boolean conToken = auth != null && auth.startsWith("Bearer ");
        return escribir(exchange, HttpStatus.UNAUTHORIZED, conToken ? "TOKEN_INVALIDO" : "NO_AUTENTICADO",
                conToken ? "Tu sesión no es válida o expiró. Inicia sesión nuevamente." : "Debes iniciar sesión", null);
    }

    public Mono<Void> accesoDenegado(ServerWebExchange exchange, AccessDeniedException e) {
        return escribir(exchange, HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", "No tienes permisos para realizar esta acción", null);
    }

    public Mono<Void> escribir(ServerWebExchange exchange, HttpStatus status, String codigo, String mensaje, Map<String, String> cabeceras) {
        ServerHttpResponse res = exchange.getResponse();
        if (res.isCommitted()) {
            return Mono.empty();
        }
        res.setStatusCode(status);
        res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        if (cabeceras != null) {
            cabeceras.forEach((k, v) -> res.getHeaders().set(k, v));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("codigo", codigo);
        body.put("mensaje", mensaje);
        body.put("ruta", exchange.getRequest().getPath().value());
        try {
            return res.writeWith(Mono.just(res.bufferFactory().wrap(om.writeValueAsBytes(body))));
        } catch (Exception ex) {
            return Mono.error(ex);
        }
    }
}
