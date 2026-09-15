package cl.licitawatch.ventas.service;

import cl.licitawatch.ventas.dto.SuscripcionResponse;
import cl.licitawatch.ventas.entity.EstadoSuscripcion;
import cl.licitawatch.ventas.entity.NombrePlan;
import cl.licitawatch.ventas.entity.Suscripcion;
import cl.licitawatch.ventas.repository.SuscripcionRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SuscripcionService {

    private final SuscripcionRepository suscripcionRepository;

    public SuscripcionService(SuscripcionRepository suscripcionRepository) {
        this.suscripcionRepository = suscripcionRepository;
    }

    /**
     * El plan Estandar es gratuito y viene activo por defecto en toda cuenta
     * (seccion "Modelo de negocio" de la PPT: solo Premium se contrata). Por
     * eso, si el usuario no tiene ninguna suscripcion de pago actualmente
     * ACTIVA (nunca compro Premium, o su Premium vencio), se reporta un
     * Estandar activo "virtual" sin necesidad de una fila en la base de
     * datos - nadie tiene que "comprar" el plan gratuito.
     *
     * No hay un job/scheduler que vaya marcando suscripciones vencidas: el
     * estado efectivo se calcula (y persiste) de forma perezosa cada vez que
     * se consulta. Es la opcion mas simple para el alcance de MVP - sin
     * infraestructura de scheduling adicional.
     */
    @Transactional
    public SuscripcionResponse obtenerVigente(Long usuarioId) {
        List<Suscripcion> suscripciones = suscripcionRepository.findByUsuarioIdOrderByCreadoEnDesc(usuarioId);

        return suscripciones.stream()
                .peek(this::actualizarSiVencio)
                .filter(s -> s.getEstado() == EstadoSuscripcion.ACTIVA)
                .max(Comparator.comparing(Suscripcion::getFechaVencimiento, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::aRespuesta)
                .orElseGet(() -> respuestaEstandarPorDefecto(usuarioId));
    }

    private void actualizarSiVencio(Suscripcion s) {
        if (s.getEstado() == EstadoSuscripcion.ACTIVA && s.getFechaVencimiento() != null
                && s.getFechaVencimiento().isBefore(LocalDate.now())) {
            s.setEstado(EstadoSuscripcion.VENCIDA);
            suscripcionRepository.save(s);
        }
    }

    private SuscripcionResponse aRespuesta(Suscripcion s) {
        return new SuscripcionResponse(s.getUsuarioId(), s.getPlan().getNombre().name(), s.getEstado().name(),
                s.getFechaInicio(), s.getFechaVencimiento());
    }

    private SuscripcionResponse respuestaEstandarPorDefecto(Long usuarioId) {
        return new SuscripcionResponse(usuarioId, NombrePlan.ESTANDAR.name(), EstadoSuscripcion.ACTIVA.name(), null, null);
    }
}
