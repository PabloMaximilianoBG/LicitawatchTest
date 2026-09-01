package cl.licitawatch.usuariosms.service;

import cl.licitawatch.usuariosms.config.RabbitConfig;
import cl.licitawatch.usuariosms.entity.PreferenciaAlerta;
import cl.licitawatch.usuariosms.entity.Usuario;
import cl.licitawatch.usuariosms.event.PreferenciaActualizadaEvent;
import cl.licitawatch.usuariosms.repository.PreferenciaAlertaRepository;
import cl.licitawatch.usuariosms.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PreferenciaAlertaService {

    private final PreferenciaAlertaRepository preferenciaAlertaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RabbitTemplate rabbitTemplate;

    public List<PreferenciaAlerta> listarPorUsuario(Long usuarioId) {
        return preferenciaAlertaRepository.findAll().stream()
                .filter(p -> p.getUsuario() != null && usuarioId.equals(p.getUsuario().getId()))
                .toList();
    }

    public PreferenciaAlerta crear(Long usuarioId, PreferenciaAlerta preferencia) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario " + usuarioId + " no existe"));
        preferencia.setUsuario(usuario);
        PreferenciaAlerta guardada = preferenciaAlertaRepository.save(preferencia);
        publicar(guardada);
        return guardada;
    }

    public PreferenciaAlerta actualizar(Long id, PreferenciaAlerta cambios) {
        PreferenciaAlerta existente = preferenciaAlertaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Preferencia " + id + " no existe"));
        existente.setRubro(cambios.getRubro());
        existente.setMonto(cambios.getMonto());
        existente.setRegion(cambios.getRegion());
        existente.setCanal(cambios.getCanal());
        existente.setFrecuencia(cambios.getFrecuencia());
        PreferenciaAlerta guardada = preferenciaAlertaRepository.save(existente);
        publicar(guardada);
        return guardada;
    }

    public void eliminar(Long id) {
        preferenciaAlertaRepository.deleteById(id);
    }

    private void publicar(PreferenciaAlerta p) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EVENTS_EXCHANGE,
                RabbitConfig.ROUTING_PREFERENCIA_ACTUALIZADA,
                new PreferenciaActualizadaEvent(
                        p.getId(),
                        p.getUsuario().getId(),
                        p.getRubro(),
                        p.getRegion(),
                        p.getMonto(),
                        p.getCanal(),
                        p.getFrecuencia()
                )
        );
    }
}
