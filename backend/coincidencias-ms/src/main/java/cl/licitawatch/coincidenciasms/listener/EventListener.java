package cl.licitawatch.coincidenciasms.listener;

import cl.licitawatch.coincidenciasms.config.RabbitConfig;
import cl.licitawatch.coincidenciasms.event.LicitacionCreadaEvent;
import cl.licitawatch.coincidenciasms.event.PreferenciaActualizadaEvent;
import cl.licitawatch.coincidenciasms.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventListener {

    private final MatchingService matchingService;

    @RabbitListener(queues = RabbitConfig.QUEUE_LICITACION_CREADA)
    public void onLicitacionCreada(LicitacionCreadaEvent event) {
        matchingService.onLicitacionCreada(event);
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_PREFERENCIA_ACTUALIZADA)
    public void onPreferenciaActualizada(PreferenciaActualizadaEvent event) {
        matchingService.onPreferenciaActualizada(event);
    }
}
