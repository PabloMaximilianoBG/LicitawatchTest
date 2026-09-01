package cl.licitawatch.coincidenciasms.service;

import cl.licitawatch.coincidenciasms.config.RabbitConfig;
import cl.licitawatch.coincidenciasms.entity.Coincidencia;
import cl.licitawatch.coincidenciasms.entity.LicitacionLocal;
import cl.licitawatch.coincidenciasms.entity.PreferenciaLocal;
import cl.licitawatch.coincidenciasms.entity.Regla;
import cl.licitawatch.coincidenciasms.event.CoincidenciaCreadaEvent;
import cl.licitawatch.coincidenciasms.event.LicitacionCreadaEvent;
import cl.licitawatch.coincidenciasms.event.PreferenciaActualizadaEvent;
import cl.licitawatch.coincidenciasms.repository.CoincidenciaRepository;
import cl.licitawatch.coincidenciasms.repository.LicitacionLocalRepository;
import cl.licitawatch.coincidenciasms.repository.PreferenciaLocalRepository;
import cl.licitawatch.coincidenciasms.repository.ReglaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Motor de coincidencia: scoring ponderado escrito a mano (sin Drools ni motores externos),
 * usando los pesos configurables en la tabla Regla. Solo trabaja sobre las copias locales
 * (LicitacionLocal / PreferenciaLocal) llenadas por eventos de RabbitMQ.
 */
@Service
@RequiredArgsConstructor
public class MatchingService {

    private static final int UMBRAL_NOTIFICACION = 60;

    private final LicitacionLocalRepository licitacionLocalRepository;
    private final PreferenciaLocalRepository preferenciaLocalRepository;
    private final CoincidenciaRepository coincidenciaRepository;
    private final ReglaRepository reglaRepository;
    private final RabbitTemplate rabbitTemplate;

    public void onLicitacionCreada(LicitacionCreadaEvent event) {
        LicitacionLocal local = licitacionLocalRepository.findById(event.getLicitacionId())
                .orElse(new LicitacionLocal());
        local.setId(event.getLicitacionId());
        local.setCodigo(event.getCodigo());
        local.setRubro(event.getRubro());
        local.setRegion(event.getRegion());
        local.setMonto(event.getMonto());
        local.setEstado(event.getEstado());
        licitacionLocalRepository.save(local);

        for (PreferenciaLocal preferencia : preferenciaLocalRepository.findAll()) {
            recalcular(local, preferencia);
        }
    }

    public void onPreferenciaActualizada(PreferenciaActualizadaEvent event) {
        PreferenciaLocal local = preferenciaLocalRepository.findById(event.getPreferenciaId())
                .orElse(new PreferenciaLocal());
        local.setId(event.getPreferenciaId());
        local.setUsuarioId(event.getUsuarioId());
        local.setRubro(event.getRubro());
        local.setRegion(event.getRegion());
        local.setMonto(event.getMonto());
        preferenciaLocalRepository.save(local);

        for (LicitacionLocal licitacion : licitacionLocalRepository.findAll()) {
            recalcular(licitacion, local);
        }
    }

    private void recalcular(LicitacionLocal licitacion, PreferenciaLocal preferencia) {
        Map<String, Integer> pesos = reglaRepository.findAll().stream()
                .collect(Collectors.toMap(Regla::getCriterio, Regla::getPeso, (a, b) -> a));

        int score = 0;
        if (coincide(licitacion.getRubro(), preferencia.getRubro())) {
            score += pesos.getOrDefault("rubro", 0);
        }
        if (coincide(licitacion.getRegion(), preferencia.getRegion())) {
            score += pesos.getOrDefault("region", 0);
        }
        score += puntajeMonto(licitacion.getMonto(), preferencia.getMonto(), pesos.getOrDefault("monto", 0));

        String nivel = score >= 75 ? "ALTA" : score >= 50 ? "MEDIA" : "BAJA";

        Coincidencia coincidencia = coincidenciaRepository
                .findByLicitacionIdAndUsuarioId(licitacion.getId(), preferencia.getUsuarioId())
                .orElseGet(Coincidencia::new);
        coincidencia.setLicitacionId(licitacion.getId());
        coincidencia.setUsuarioId(preferencia.getUsuarioId());
        coincidencia.setScore(score);
        coincidencia.setEstado(nivel);
        Coincidencia guardada = coincidenciaRepository.save(coincidencia);

        if (score >= UMBRAL_NOTIFICACION) {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EVENTS_EXCHANGE,
                    RabbitConfig.ROUTING_COINCIDENCIA_CREADA,
                    new CoincidenciaCreadaEvent(guardada.getId(), guardada.getUsuarioId(),
                            guardada.getLicitacionId(), guardada.getScore(), guardada.getEstado())
            );
        }
    }

    private boolean coincide(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    private int puntajeMonto(BigDecimal montoLicitacion, BigDecimal montoPreferencia, int pesoMonto) {
        if (montoLicitacion == null || montoPreferencia == null) {
            return 0;
        }
        if (montoLicitacion.compareTo(montoPreferencia) <= 0) {
            return pesoMonto;
        }
        // hasta un 20% sobre el monto de referencia: coincidencia parcial
        BigDecimal margen = montoPreferencia.multiply(BigDecimal.valueOf(1.2));
        if (montoLicitacion.compareTo(margen) <= 0) {
            return pesoMonto / 2;
        }
        return 0;
    }
}
