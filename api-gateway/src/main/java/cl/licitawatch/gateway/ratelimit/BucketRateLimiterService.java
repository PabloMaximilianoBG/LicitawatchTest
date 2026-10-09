package cl.licitawatch.gateway.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

/** Token bucket en memoria (Bucket4j) con ventana fija por minuto. */
@Service
public class BucketRateLimiterService implements RateLimiterService {
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(10)).maximumSize(100_000).build();
    private final int general;
    private final int auth;
    private final int asistente;

    public BucketRateLimiterService(@Value("${licitawatch.rate-limit.general-per-minute:300}") int general,
                                    @Value("${licitawatch.rate-limit.auth-per-minute:30}") int auth,
                                    @Value("${licitawatch.rate-limit.asistente-per-minute:20}") int asistente) {
        this.general = general;
        this.auth = auth;
        this.asistente = asistente;
    }

    @Override
    public Resultado consumir(Categoria categoria, String cliente) {
        int limite = switch (categoria) {
            case AUTH -> auth;
            case ASISTENTE -> asistente;
            case GENERAL -> general;
        };
        Bucket b = buckets.get(categoria + ":" + cliente, k -> Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(limite).refillIntervally(limite, Duration.ofMinutes(1)).build()).build());
        ConsumptionProbe p = b.tryConsumeAndReturnRemaining(1);
        return new Resultado(p.isConsumed(), Math.max(1, Duration.ofNanos(p.getNanosToWaitForRefill()).toSeconds()));
    }
}
