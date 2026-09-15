package cl.licitawatch.usuarios.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import cl.licitawatch.usuarios.dto.ErrorResponse;

/**
 * Rate limiting en memoria (sin Redis) para el endpoint de login, por IP de origen.
 * Mitiga fuerza bruta de credenciales, uno de los 3 riesgos destacados en la arquitectura.
 */
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String RUTA_LOGIN = "/api/auth/login";

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final int capacidad;
    private final int periodoMinutos;

    public LoginRateLimitFilter(
            ObjectMapper objectMapper,
            @Value("${licitawatch.rate-limit.login.capacidad}") int capacidad,
            @Value("${licitawatch.rate-limit.login.periodo-minutos}") int periodoMinutos) {
        this.objectMapper = objectMapper;
        this.capacidad = capacidad;
        this.periodoMinutos = periodoMinutos;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !RUTA_LOGIN.equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(ip, k -> Bucket.builder()
                .addLimit(Bandwidth.classic(capacidad, Refill.greedy(capacidad, Duration.ofMinutes(periodoMinutos))))
                .build());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            ErrorResponse body = new ErrorResponse(429,
                    "Demasiados intentos de inicio de sesion, intenta nuevamente en un minuto",
                    request.getRequestURI(), LocalDateTime.now(), null);
            response.getWriter().write(objectMapper.writeValueAsString(body));
        }
    }
}
