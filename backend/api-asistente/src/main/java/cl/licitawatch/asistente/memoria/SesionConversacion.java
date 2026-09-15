package cl.licitawatch.asistente.memoria;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

class SesionConversacion {

    private final Deque<Turno> turnos = new ArrayDeque<>();
    private volatile Instant ultimoAcceso = Instant.now();

    synchronized void agregar(Turno turno, int maxTurnos) {
        turnos.addLast(turno);
        while (turnos.size() > maxTurnos) {
            turnos.removeFirst();
        }
        ultimoAcceso = Instant.now();
    }

    synchronized java.util.List<Turno> obtener() {
        ultimoAcceso = Instant.now();
        return java.util.List.copyOf(turnos);
    }

    boolean expirada(int ttlMinutos) {
        return ultimoAcceso.isBefore(Instant.now().minusSeconds(ttlMinutos * 60L));
    }
}
