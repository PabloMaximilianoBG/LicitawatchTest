package cl.licitawatch.gateway.ratelimit;

/** Límite de solicitudes por cliente y categoría. */
public interface RateLimiterService {
    enum Categoria { AUTH, ASISTENTE, GENERAL }

    record Resultado(boolean permitido, long esperarSegundos) {
    }

    Resultado consumir(Categoria categoria, String cliente);
}
