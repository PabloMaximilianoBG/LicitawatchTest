package cl.licitawatch.gateway.filter;

import cl.licitawatch.gateway.security.RespuestasJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;

/** Errores propios del gateway en JSON uniforme (ej: un BFF caído -> 503). */
@Slf4j
@Component
@Order(-2)
@RequiredArgsConstructor
public class GatewayErrorHandler implements ErrorWebExceptionHandler {
    private final RespuestasJson respuestas;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof ResponseStatusException rse && rse.getStatusCode().value() == 404) {
            return respuestas.escribir(exchange, HttpStatus.NOT_FOUND, "RUTA_NO_ENCONTRADA", "Recurso no encontrado", null);
        }
        if (ex instanceof ConnectException || ex.getCause() instanceof ConnectException
                || ex.getClass().getName().contains("AnnotatedConnectException")) {
            return respuestas.escribir(exchange, HttpStatus.SERVICE_UNAVAILABLE, "SERVICIO_NO_DISPONIBLE",
                    "El servicio solicitado no está disponible en este momento.", null);
        }
        if (ex instanceof ResponseStatusException rse && rse.getStatusCode().value() == 504) {
            return respuestas.escribir(exchange, HttpStatus.GATEWAY_TIMEOUT, "TIEMPO_AGOTADO", "El servicio tardó demasiado en responder.", null);
        }
        log.error("Error en el gateway para {}", exchange.getRequest().getPath(), ex);
        return respuestas.escribir(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Ocurrió un error interno.", null);
    }
}
