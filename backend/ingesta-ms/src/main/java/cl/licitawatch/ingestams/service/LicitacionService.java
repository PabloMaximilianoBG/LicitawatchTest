package cl.licitawatch.ingestams.service;

import cl.licitawatch.ingestams.config.RabbitConfig;
import cl.licitawatch.ingestams.entity.Item;
import cl.licitawatch.ingestams.entity.Licitacion;
import cl.licitawatch.ingestams.event.LicitacionCreadaEvent;
import cl.licitawatch.ingestams.repository.LicitacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LicitacionService {

    private final LicitacionRepository licitacionRepository;
    private final RabbitTemplate rabbitTemplate;

    public List<Licitacion> listar() {
        return licitacionRepository.findAll();
    }

    public Licitacion buscar(Long id) {
        return licitacionRepository.findById(id).orElse(null);
    }

    public Licitacion crear(Licitacion licitacion) {
        if (licitacion.getItems() != null) {
            for (Item item : licitacion.getItems()) {
                item.setLicitacion(licitacion);
            }
        }
        Licitacion guardada = licitacionRepository.save(licitacion);

        rabbitTemplate.convertAndSend(
                RabbitConfig.EVENTS_EXCHANGE,
                RabbitConfig.ROUTING_LICITACION_CREADA,
                new LicitacionCreadaEvent(
                        guardada.getId(),
                        guardada.getCodigo(),
                        guardada.getRubro(),
                        guardada.getRegion(),
                        guardada.getMonto(),
                        guardada.getEstado(),
                        guardada.getFechaCierre()
                )
        );

        return guardada;
    }
}
