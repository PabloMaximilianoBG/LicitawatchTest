package cl.licitawatch.licitaciones.bd.service.impl;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.common.exception.ResourceNotFoundException;
import cl.licitawatch.licitaciones.bd.dto.request.PostulacionBdRequest;
import cl.licitawatch.licitaciones.bd.dto.response.AdjudicacionBdResponse;
import cl.licitawatch.licitaciones.bd.dto.response.PostulacionBdResponse;
import cl.licitawatch.licitaciones.bd.entity.EstadoPostulacion;
import cl.licitawatch.licitaciones.bd.entity.Licitacion;
import cl.licitawatch.licitaciones.bd.entity.Postulacion;
import cl.licitawatch.licitaciones.bd.mapper.LicitacionBdMapper;
import cl.licitawatch.licitaciones.bd.repository.EstadoLicitacionRepository;
import cl.licitawatch.licitaciones.bd.repository.EstadoPostulacionRepository;
import cl.licitawatch.licitaciones.bd.repository.LicitacionRepository;
import cl.licitawatch.licitaciones.bd.repository.PostulacionRepository;
import cl.licitawatch.licitaciones.bd.service.PostulacionDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostulacionDataServiceImpl implements PostulacionDataService {
    private final PostulacionRepository postulacionRepository;
    private final LicitacionRepository licitacionRepository;
    private final EstadoPostulacionRepository estadoPostulacionRepository;
    private final EstadoLicitacionRepository estadoLicitacionRepository;
    private final LicitacionBdMapper mapper;

    @Override
    public PaginaResponse<PostulacionBdResponse> listar(Integer licitacionId, Integer pymeId, String estado, int page, int size) {
        Specification<Postulacion> spec = (root, q, cb) -> {
            List<jakarta.persistence.criteria.Predicate> p = new ArrayList<>();
            if (licitacionId != null) {
                p.add(cb.equal(root.get("licitacion").get("id"), licitacionId));
            }
            if (pymeId != null) {
                p.add(cb.equal(root.get("pymeId"), pymeId));
            }
            if (estado != null && !estado.isBlank()) {
                p.add(cb.equal(cb.lower(root.get("estadoPostulacion").get("nombre")), estado.toLowerCase()));
            }
            return cb.and(p.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        var pagina = postulacionRepository.findAll(spec, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200),
                Sort.by(Sort.Direction.DESC, "fechaPostulacion").and(Sort.by(Sort.Direction.DESC, "id"))));
        return PaginaResponse.de(pagina, mapper::postulacion);
    }

    @Override
    public List<PostulacionBdResponse> deLicitacion(Integer licitacionId) {
        return postulacionRepository.findByLicitacionIdOrderByFechaPostulacionAscIdAsc(licitacionId).stream().map(mapper::postulacion).toList();
    }

    @Override
    public PostulacionBdResponse obtener(Integer id) {
        return mapper.postulacion(postulacion(id));
    }

    @Override
    @Transactional
    public PostulacionBdResponse crear(PostulacionBdRequest r) {
        Licitacion l = licitacionRepository.findById(r.licitacionId())
                .orElseThrow(() -> new ResourceNotFoundException("LICITACION_NO_ENCONTRADA", "La licitación no existe"));
        Postulacion p = Postulacion.builder().licitacion(l).pymeId(r.pymeId())
                .mensaje(r.mensaje() == null || r.mensaje().isBlank() ? null : r.mensaje().trim())
                .fechaPostulacion(LocalDate.now()).estadoPostulacion(estado("Pendiente")).updatedAt(LocalDateTime.now()).build();
        return mapper.postulacion(postulacionRepository.save(p));
    }

    @Override
    @Transactional
    public PostulacionBdResponse cambiarEstado(Integer id, String estado) {
        Postulacion p = postulacion(id);
        p.setEstadoPostulacion(estado(estado));
        p.setUpdatedAt(LocalDateTime.now());
        return mapper.postulacion(p);
    }

    @Override
    public long contarDesde(Integer pymeId, LocalDate desde) {
        return postulacionRepository.countByPymeIdAndFechaPostulacionGreaterThanEqual(pymeId, desde);
    }

    @Override
    public boolean existe(Integer licitacionId, Integer pymeId) {
        return postulacionRepository.existsByLicitacionIdAndPymeId(licitacionId, pymeId);
    }

    @Override
    @Transactional
    public AdjudicacionBdResponse adjudicar(Integer licitacionId, Integer postulacionId) {
        Postulacion elegida = postulacion(postulacionId);
        if (!elegida.getLicitacion().getId().equals(licitacionId)) {
            throw new BadRequestException("POSTULACION_DE_OTRA_LICITACION", "La postulación no pertenece a la licitación");
        }
        LocalDateTime ahora = LocalDateTime.now();
        EstadoPostulacion aprobada = estado("Aprobada");
        EstadoPostulacion rechazada = estado("Rechazada");
        List<PostulacionBdResponse> rechazadas = new ArrayList<>();
        for (Postulacion p : postulacionRepository.findByLicitacionIdOrderByFechaPostulacionAscIdAsc(licitacionId)) {
            if (p.getId().equals(postulacionId)) {
                p.setEstadoPostulacion(aprobada);
                p.setUpdatedAt(ahora);
            } else if ("Pendiente".equalsIgnoreCase(p.getEstadoPostulacion().getNombre())) {
                p.setEstadoPostulacion(rechazada);
                p.setUpdatedAt(ahora);
                rechazadas.add(mapper.postulacion(p));
            }
        }
        elegida.getLicitacion().setEstadoLicitacion(estadoLicitacionRepository.findByNombreIgnoreCase("Adjudicada")
                .orElseThrow(() -> new IllegalStateException("Falta el estado Adjudicada")));
        return new AdjudicacionBdResponse(mapper.postulacion(elegida), rechazadas);
    }

    private Postulacion postulacion(Integer id) {
        return postulacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("POSTULACION_NO_ENCONTRADA", "La postulación no existe"));
    }

    private EstadoPostulacion estado(String nombre) {
        return estadoPostulacionRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new BadRequestException("ESTADO_INVALIDO", "Estado de postulación inválido: " + nombre));
    }
}
