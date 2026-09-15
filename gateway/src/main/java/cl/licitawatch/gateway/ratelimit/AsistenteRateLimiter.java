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
 * Rate limiting en memoria para las rutas de API Asistente, por usuario
 * (segun el "sub" del JWT) - protege el Free Tier de Groq (seccion 6).
 */
@Component
public class AsistenteRateLimiter {

    private final Map<Long, Bucket> buckets = new ConcurrentHashMap<>();
    private final int capacidad;
    private final int periodoMinutos;

    public AsistenteRateLimiter(
            @Value("${licitawatch.rate-limit.asistente.capacidad}") int capacidad,
            @Value("${licitawatch.rate-limit.asistente.periodo-minutos}") int periodoMinutos) {
        this.capacidad = capacidad;
        this.periodoMinutos = periodoMinutos;
    }

    public boolean permitir(Long usuarioId) {
        Bucket bucket = buckets.computeIfAbsent(usuarioId, k -> Bucket.builder()
                .addLimit(Bandwidth.classic(capacidad, Refill.greedy(capacidad, Duration.ofMinutes(periodoMinutos))))
                .build());
        return bucket.tryConsume(1);
    }
}
