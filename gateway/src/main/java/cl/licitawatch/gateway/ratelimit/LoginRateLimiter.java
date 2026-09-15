package cl.licitawatch.gateway.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Rate limiting en memoria (sin Redis) para POST /api/auth/login, por IP de
 * origen - mitiga fuerza bruta de credenciales (seccion 6, uno de los 3
 * riesgos destacados en la arquitectura). API Usuarios aplica su propio
 * limite equivalente si se le llama directo (defensa en profundidad); este
 * es el que protege a todo el sistema cuando se entra por el Gateway.
 */
@Component
public class LoginRateLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int capacidad;
    private final int periodoMinutos;

    public LoginRateLimiter(
            @Value("${licitawatch.rate-limit.login.capacidad}") int capacidad,
            @Value("${licitawatch.rate-limit.login.periodo-minutos}") int periodoMinutos) {
        this.capacidad = capacidad;
        this.periodoMinutos = periodoMinutos;
    }

    public boolean permitir(String ip) {
        Bucket bucket = buckets.computeIfAbsent(ip, k -> Bucket.builder()
                .addLimit(Bandwidth.classic(capacidad, Refill.greedy(capacidad, Duration.ofMinutes(periodoMinutos))))
                .build());
        return bucket.tryConsume(1);
    }
}
