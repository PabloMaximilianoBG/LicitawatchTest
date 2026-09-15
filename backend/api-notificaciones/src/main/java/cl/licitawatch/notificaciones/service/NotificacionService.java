package cl.licitawatch.notificaciones.service;

import cl.licitawatch.notificaciones.dto.NotificacionIngestaRequest;
import cl.licitawatch.notificaciones.dto.NotificacionResponse;
import cl.licitawatch.notificaciones.dto.UsuarioInternoResponse;
import cl.licitawatch.notificaciones.entity.EstadoNotificacion;
import cl.licitawatch.notificaciones.entity.Notificacion;
import cl.licitawatch.notificaciones.repository.NotificacionRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private final NotificacionRepository notificacionRepository;
    private final UsuarioClient usuarioClient;
    private final EmailService emailService;

    public NotificacionService(NotificacionRepository notificacionRepository, UsuarioClient usuarioClient, EmailService emailService) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioClient = usuarioClient;
        this.emailService = emailService;
    }

    @Transactional
    public NotificacionResponse recibir(NotificacionIngestaRequest req) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuarioId(req.usuarioId());
        notificacion.setTipo(req.tipo());
        notificacion.setCanal(req.canal());
        notificacion.setEstado(EstadoNotificacion.PENDIENTE);
        notificacionRepository.save(notificacion);

        Optional<UsuarioInternoResponse> usuario = usuarioClient.resolver(req.usuarioId());
        if (usuario.isEmpty()) {
            log.warn("Notificacion {} queda PENDIENTE: no se pudo resolver el email del usuario {}", notificacion.getId(), req.usuarioId());
            return new NotificacionResponse(notificacion.getId(), notificacion.getEstado().name());
        }

        boolean enviado = emailService.enviar(usuario.get().email(), req.asunto(), req.mensaje());
        notificacion.setEstado(enviado ? EstadoNotificacion.ENVIADO : EstadoNotificacion.PENDIENTE);
        notificacionRepository.save(notificacion);

        return new NotificacionResponse(notificacion.getId(), notificacion.getEstado().name());
    }
}
