package cl.licitawatch.asistente.memoria;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Memoria conversacional en proceso (seccion 8.1 del diseno): un mapa
 * usuario_id -> ultimos N turnos, con expiracion por inactividad. No se
 * persiste en ninguna base de datos a proposito - API Asistente no tiene
 * (ni deberia tener) BD propia de negocio, y perder el historial en curso
 * si el servicio se reinicia es una consecuencia aceptada de esa decision,
 * no un olvido. La expiracion se revisa de forma perezosa (al acceder),
 * sin un scheduler en segundo plano.
 */
@Service
public class ConversacionMemoriaService {

    private final ConcurrentMap<Long, SesionConversacion> sesiones = new ConcurrentHashMap<>();
    private final int ttlMinutos;
    private final int maxTurnos;

    public ConversacionMemoriaService(
            @Value("${licitawatch.asistente.memoria.ttl-minutos}") int ttlMinutos,
            @Value("${licitawatch.asistente.memoria.max-turnos}") int maxTurnos) {
        this.ttlMinutos = ttlMinutos;
        this.maxTurnos = maxTurnos;
    }

    public List<Turno> obtenerHistorial(Long usuarioId) {
        SesionConversacion sesion = sesiones.get(usuarioId);
        if (sesion == null || sesion.expirada(ttlMinutos)) {
            sesiones.remove(usuarioId);
            return List.of();
        }
        return sesion.obtener();
    }

    public void agregarTurno(Long usuarioId, String rol, String contenido) {
        SesionConversacion sesion = sesiones.compute(usuarioId, (id, existente) ->
                (existente == null || existente.expirada(ttlMinutos)) ? new SesionConversacion() : existente);
        sesion.agregar(new Turno(rol, contenido), maxTurnos);
    }
}
