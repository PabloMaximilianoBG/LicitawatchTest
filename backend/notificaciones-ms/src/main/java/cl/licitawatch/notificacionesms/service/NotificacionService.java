package cl.licitawatch.notificacionesms.service;

import cl.licitawatch.notificacionesms.entity.Auditoria;
import cl.licitawatch.notificacionesms.entity.Notificacion;
import cl.licitawatch.notificacionesms.event.CoincidenciaCreadaEvent;
import cl.licitawatch.notificacionesms.repository.AuditoriaRepository;
import cl.licitawatch.notificacionesms.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Registra la notificación y su auditoría cuando llega una coincidencia relevante.
 * El envío real de correo (Mailtrap) queda pendiente hasta tener las credenciales SMTP;
 * por ahora la notificación queda con estado PENDIENTE_ENVIO.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final AuditoriaRepository auditoriaRepository;

    public void procesarCoincidencia(CoincidenciaCreadaEvent event) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuarioId(event.getUsuarioId());
        notificacion.setLicitacionId(event.getLicitacionId());
        notificacion.setCanal("Correo");
        notificacion.setEstado("PENDIENTE_ENVIO");
        notificacion.setTs(LocalDateTime.now());
        notificacionRepository.save(notificacion);

        Auditoria auditoria = new Auditoria();
        auditoria.setUsuarioId(event.getUsuarioId());
        auditoria.setEvento("COINCIDENCIA_DETECTADA score=" + event.getScore() + " estado=" + event.getEstado());
        auditoria.setTs(LocalDateTime.now());
        auditoriaRepository.save(auditoria);

        log.info("Notificación registrada para usuario {} (licitación {}, score {}). Envío por Mailtrap pendiente de credenciales.",
                event.getUsuarioId(), event.getLicitacionId(), event.getScore());
    }

    public List<Notificacion> listarPorUsuario(Long usuarioId) {
        return notificacionRepository.findAll().stream()
                .filter(n -> usuarioId.equals(n.getUsuarioId()))
                .toList();
    }
}
