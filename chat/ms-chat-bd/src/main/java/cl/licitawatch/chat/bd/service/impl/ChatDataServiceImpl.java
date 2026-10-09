package cl.licitawatch.chat.bd.service.impl;

import cl.licitawatch.chat.bd.dto.request.ConversacionBdRequest;
import cl.licitawatch.chat.bd.dto.request.MensajeBdRequest;
import cl.licitawatch.chat.bd.dto.response.ConversacionBdResponse;
import cl.licitawatch.chat.bd.dto.response.MensajeBdResponse;
import cl.licitawatch.chat.bd.entity.Conversacion;
import cl.licitawatch.chat.bd.entity.Mensaje;
import cl.licitawatch.chat.bd.repository.ConversacionRepository;
import cl.licitawatch.chat.bd.repository.MensajeRepository;
import cl.licitawatch.chat.bd.service.ChatDataService;
import cl.licitawatch.common.exception.ConflictException;
import cl.licitawatch.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatDataServiceImpl implements ChatDataService {
    private final ConversacionRepository conversacionRepository;
    private final MensajeRepository mensajeRepository;

    @Override
    @Transactional
    public ConversacionBdResponse crear(ConversacionBdRequest r) {
        if (conversacionRepository.existsByPostulacionId(r.postulacionId())) {
            throw new ConflictException("CONVERSACION_EXISTE", "Ya existe una conversación para esta postulación");
        }
        Conversacion c = conversacionRepository.save(Conversacion.builder().postulacionId(r.postulacionId())
                .licitadorId(r.licitadorId()).pymeId(r.pymeId()).createdAt(LocalDateTime.now()).build());
        return dto(c, null);
    }

    @Override
    public ConversacionBdResponse obtener(Integer id, Integer lectorId) {
        return dto(conversacion(id), lectorId);
    }

    @Override
    public ConversacionBdResponse porPostulacion(Integer postulacionId, Integer lectorId) {
        return conversacionRepository.findByPostulacionId(postulacionId).map(c -> dto(c, lectorId))
                .orElseThrow(() -> new ResourceNotFoundException("CONVERSACION_NO_ENCONTRADA", "Aún no hay conversación para esta postulación"));
    }

    @Override
    public List<ConversacionBdResponse> listar(Integer licitadorId, Integer pymeId, Integer lectorId) {
        List<Conversacion> lista = licitadorId != null ? conversacionRepository.findByLicitadorIdOrderByCreatedAtDesc(licitadorId)
                : pymeId != null ? conversacionRepository.findByPymeIdOrderByCreatedAtDesc(pymeId) : List.of();
        return lista.stream().map(c -> dto(c, lectorId)).toList();
    }

    @Override
    @Transactional
    public int eliminarPorPostulaciones(List<Integer> postulacionIds) {
        List<Conversacion> lista = conversacionRepository.findByPostulacionIdIn(postulacionIds);
        if (lista.isEmpty()) {
            return 0;
        }
        mensajeRepository.eliminarDeConversaciones(lista.stream().map(Conversacion::getId).toList());
        conversacionRepository.deleteAll(lista);
        return lista.size();
    }

    @Override
    public List<MensajeBdResponse> mensajes(Integer conversacionId, Integer despuesDeId) {
        conversacion(conversacionId);
        return mensajeRepository.findByConversacionIdAndIdGreaterThanOrderByIdAsc(conversacionId, despuesDeId == null ? 0 : despuesDeId)
                .stream().map(m -> dto(m, conversacionId)).toList();
    }

    @Override
    @Transactional
    public MensajeBdResponse enviar(Integer conversacionId, MensajeBdRequest r) {
        Mensaje m = mensajeRepository.save(Mensaje.builder().conversacion(conversacion(conversacionId)).emisorId(r.emisorId())
                .contenido(r.contenido().trim()).enviadoAt(LocalDateTime.now()).leido(false).build());
        return dto(m, conversacionId);
    }

    @Override
    @Transactional
    public int marcarLeidos(Integer conversacionId, Integer lectorId) {
        return mensajeRepository.marcarLeidos(conversacionId, lectorId);
    }

    @Override
    public long noLeidos(Integer conversacionId, Integer lectorId) {
        return mensajeRepository.countByConversacionIdAndEmisorIdNotAndLeidoFalse(conversacionId, lectorId);
    }

    private ConversacionBdResponse dto(Conversacion c, Integer lectorId) {
        var ultimo = mensajeRepository.findFirstByConversacionIdOrderByIdDesc(c.getId());
        return ConversacionBdResponse.builder().id(c.getId()).postulacionId(c.getPostulacionId()).licitadorId(c.getLicitadorId())
                .pymeId(c.getPymeId()).createdAt(c.getCreatedAt())
                .ultimoMensaje(ultimo.map(Mensaje::getContenido).orElse(null))
                .ultimoMensajeAt(ultimo.map(Mensaje::getEnviadoAt).orElse(null))
                .noLeidos(lectorId == null ? 0 : mensajeRepository.countByConversacionIdAndEmisorIdNotAndLeidoFalse(c.getId(), lectorId))
                .build();
    }

    private static MensajeBdResponse dto(Mensaje m, Integer conversacionId) {
        return new MensajeBdResponse(m.getId(), conversacionId, m.getEmisorId(), m.getContenido(), m.getEnviadoAt(), m.getLeido());
    }

    private Conversacion conversacion(Integer id) {
        return conversacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CONVERSACION_NO_ENCONTRADA", "La conversación no existe"));
    }
}
