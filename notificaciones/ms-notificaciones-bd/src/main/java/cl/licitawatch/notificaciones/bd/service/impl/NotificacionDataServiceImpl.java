package cl.licitawatch.notificaciones.bd.service.impl;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.notificaciones.bd.dto.request.NotificacionBdRequest;
import cl.licitawatch.notificaciones.bd.dto.response.CatalogoResponse;
import cl.licitawatch.notificaciones.bd.dto.response.NotificacionBdResponse;
import cl.licitawatch.notificaciones.bd.entity.Notificacion;
import cl.licitawatch.notificaciones.bd.repository.NotificacionRepository;
import cl.licitawatch.notificaciones.bd.repository.TipoNotificacionRepository;
import cl.licitawatch.notificaciones.bd.service.NotificacionDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificacionDataServiceImpl implements NotificacionDataService {
    private final NotificacionRepository notificacionRepository;
    private final TipoNotificacionRepository tipoRepository;

    @Override
    @Transactional
    public NotificacionBdResponse registrar(NotificacionBdRequest r) {
        Notificacion n = notificacionRepository.save(Notificacion.builder().usuarioId(r.usuarioId())
                .tipoNotificacion(tipoRepository.findByNombreIgnoreCase(r.tipo())
                        .orElseThrow(() -> new BadRequestException("TIPO_INVALIDO", "Tipo de notificación inválido: " + r.tipo())))
                .canal(r.canal()).createdAt(LocalDateTime.now()).build());
        return dto(n);
    }

    @Override
    public PaginaResponse<NotificacionBdResponse> listar(Integer usuarioId, String tipo, int page, int size) {
        Specification<Notificacion> spec = (root, q, cb) -> {
            List<jakarta.persistence.criteria.Predicate> p = new ArrayList<>();
            if (usuarioId != null) {
                p.add(cb.equal(root.get("usuarioId"), usuarioId));
            }
            if (tipo != null && !tipo.isBlank()) {
                p.add(cb.equal(root.get("tipoNotificacion").get("nombre"), tipo));
            }
            return cb.and(p.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        var pagina = notificacionRepository.findAll(spec, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))));
        return PaginaResponse.de(pagina, this::dto);
    }

    @Override
    public List<CatalogoResponse> tipos() {
        return tipoRepository.findAll(Sort.by("id")).stream().map(t -> new CatalogoResponse(t.getId(), t.getNombre())).toList();
    }

    private NotificacionBdResponse dto(Notificacion n) {
        return new NotificacionBdResponse(n.getId(), n.getUsuarioId(), n.getTipoNotificacion().getNombre(), n.getCanal(), n.getCreatedAt());
    }
}
