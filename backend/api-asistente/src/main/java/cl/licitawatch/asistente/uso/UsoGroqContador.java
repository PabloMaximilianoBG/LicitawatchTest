package cl.licitawatch.asistente.uso;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * Contador simple en memoria (seccion 3.5, paso 11): permite ver cuanto se
 * esta usando el Free Tier de Groq, por usuario y en total, sin necesidad de
 * una base de datos ni de un panel de monitoreo externo. Se reinicia si el
 * servicio se reinicia - consistente con que este microservicio no tiene
 * persistencia propia.
 */
@Component
public class UsoGroqContador {

    private final AtomicLong usoGlobal = new AtomicLong();
    private final ConcurrentHashMap<Long, AtomicInteger> usoPorUsuario = new ConcurrentHashMap<>();

    public void registrar(Long usuarioId) {
        usoGlobal.incrementAndGet();
        usoPorUsuario.computeIfAbsent(usuarioId, id -> new AtomicInteger()).incrementAndGet();
    }

    public long usoGlobal() {
        return usoGlobal.get();
    }

    public int usoDe(Long usuarioId) {
        AtomicInteger contador = usoPorUsuario.get(usuarioId);
        return contador == null ? 0 : contador.get();
    }
}
