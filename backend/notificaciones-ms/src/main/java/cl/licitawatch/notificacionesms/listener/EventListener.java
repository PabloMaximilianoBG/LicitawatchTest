package cl.licitawatch.notificacionesms.listener;

import cl.licitawatch.notificacionesms.config.RabbitConfig;
import cl.licitawatch.notificacionesms.event.CoincidenciaCreadaEvent;
import cl.licitawatch.notificacionesms.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventListener {

    private final NotificacionService notificacionService;

    @RabbitListener(queues = RabbitConfig.QUEUE_COINCIDENCIA_CREADA)
    public void onCoincidenciaCreada(CoincidenciaCreadaEvent event) {
        notificacionService.procesarCoincidencia(event);
    }
}
