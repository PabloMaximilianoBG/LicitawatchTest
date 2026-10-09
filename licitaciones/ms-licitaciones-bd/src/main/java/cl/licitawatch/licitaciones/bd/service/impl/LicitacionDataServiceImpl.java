package cl.licitawatch.licitaciones.bd.service.impl;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.common.exception.ResourceNotFoundException;
import cl.licitawatch.licitaciones.bd.dto.request.ArchivosBdRequest;
import cl.licitawatch.licitaciones.bd.dto.request.LicitacionBdRequest;
import cl.licitawatch.licitaciones.bd.dto.response.CatalogosBdResponse;
import cl.licitawatch.licitaciones.bd.dto.response.EliminacionBdResponse;
import cl.licitawatch.licitaciones.bd.dto.response.LicitacionBdResponse;
import cl.licitawatch.licitaciones.bd.entity.*;
import cl.licitawatch.licitaciones.bd.mapper.LicitacionBdMapper;
import cl.licitawatch.licitaciones.bd.repository.*;
import cl.licitawatch.licitaciones.bd.service.LicitacionDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LicitacionDataServiceImpl implements LicitacionDataService {
    private final LicitacionRepository licitacionRepository;
    private final PostulacionRepository postulacionRepository;
    private final EstadoLicitacionRepository estadoLicitacionRepository;
    private final EstadoPostulacionRepository estadoPostulacionRepository;
    private final TipoArchivoRepository tipoArchivoRepository;
    private final LicitacionBdMapper mapper;

    @Override
    public PaginaResponse<LicitacionBdResponse> buscar(Filtros f) {
        Specification<Licitacion> spec = Specification.where(LicitacionSpecs.texto(f.q()))
                .and(LicitacionSpecs.igual("rubroId", f.rubroId()))
                .and(LicitacionSpecs.igual("regionId", f.regionId()))
                .and(LicitacionSpecs.igual("licitadorId", f.licitadorId()))
                .and(LicitacionSpecs.estado(f.estado()))
                .and(LicitacionSpecs.presupuestoDesde(f.presupuestoMin()))
                .and(LicitacionSpecs.presupuestoHasta(f.presupuestoMax()))
                .and(LicitacionSpecs.vigenteDesde(f.vigenteDesde()))
                .and(LicitacionSpecs.conCupo(f.conCupo()));
        Sort orden = "cierre".equalsIgnoreCase(f.orden())
                ? Sort.by(Sort.Direction.ASC, "fechaCierre").and(Sort.by("id"))
                : Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
        Page<Licitacion> pagina = licitacionRepository.findAll(spec,
                PageRequest.of(Math.max(f.page(), 0), Math.min(Math.max(f.size(), 1), 100), orden));
        Map<Integer, Long> conteos = conteos(pagina.getContent());
        return PaginaResponse.de(pagina, l -> mapper.licitacion(l, conteos.getOrDefault(l.getId(), 0L)));
    }

    @Override
    public LicitacionBdResponse obtener(Integer id) {
        Licitacion l = licitacion(id);
        return mapper.licitacion(l, postulacionRepository.countByLicitacionId(id));
    }

    @Override
    public List<LicitacionBdResponse> porIds(List<Integer> ids) {
        List<Licitacion> lista = licitacionRepository.findAllById(ids);
        Map<Integer, Long> conteos = conteos(lista);
        return lista.stream().map(l -> mapper.licitacion(l, conteos.getOrDefault(l.getId(), 0L))).toList();
    }

    @Override
    public List<LicitacionBdResponse> vencidas(LocalDate fecha) {
        return licitacionRepository.findByEstadoLicitacionNombreAndFechaCierreBefore("Abierta", fecha).stream()
                .map(l -> mapper.licitacion(l, 0)).toList();
    }

    @Override
    @Transactional
    public LicitacionBdResponse crear(LicitacionBdRequest r) {
        Licitacion l = Licitacion.builder().createdAt(LocalDateTime.now())
                .estadoLicitacion(estadoLicitacion(r.estado() != null ? r.estado() : "Abierta")).build();
        copiar(r, l);
        return mapper.licitacion(licitacionRepository.save(l), 0);
    }

    @Override
    @Transactional
    public LicitacionBdResponse actualizar(Integer id, LicitacionBdRequest r) {
        Licitacion l = licitacion(id);
        copiar(r, l);
        if (r.estado() != null) {
            l.setEstadoLicitacion(estadoLicitacion(r.estado()));
        }
        return mapper.licitacion(l, postulacionRepository.countByLicitacionId(id));
    }

    @Override
    @Transactional
    public LicitacionBdResponse cambiarEstado(Integer id, String estado) {
        Licitacion l = licitacion(id);
        l.setEstadoLicitacion(estadoLicitacion(estado));
        return mapper.licitacion(l, postulacionRepository.countByLicitacionId(id));
    }

    @Override
    @Transactional
    public LicitacionBdResponse actualizarArchivos(Integer id, ArchivosBdRequest r) {
        Licitacion l = licitacion(id);
        if (r.limpiarImagen()) {
            l.setImagenUrl(null);
        } else if (r.imagenUrl() != null) {
            l.setImagenUrl(r.imagenUrl());
        }
        if (r.limpiarArchivo()) {
            l.setArchivoUrl(null);
            l.setArchivoNombre(null);
            l.setTipoArchivo(null);
        } else if (r.archivoUrl() != null) {
            l.setArchivoUrl(r.archivoUrl());
            l.setArchivoNombre(r.archivoNombre());
            l.setTipoArchivo(tipoArchivoRepository.findByNombreIgnoreCase(r.tipoArchivo() != null ? r.tipoArchivo() : "Otro")
                    .orElseThrow(() -> new BadRequestException("TIPO_ARCHIVO_INVALIDO", "Tipo de archivo inválido")));
        }
        return mapper.licitacion(l, postulacionRepository.countByLicitacionId(id));
    }

    @Override
    @Transactional
    public EliminacionBdResponse eliminar(Integer id) {
        Licitacion l = licitacion(id);
        var postulaciones = postulacionRepository.findByLicitacionIdOrderByFechaPostulacionAscIdAsc(id);
        List<Integer> ids = postulaciones.stream().map(Postulacion::getId).toList();
        postulacionRepository.deleteAll(postulaciones);
        licitacionRepository.delete(l);
        return new EliminacionBdResponse(id, ids);
    }

    @Override
    public CatalogosBdResponse catalogos() {
        return new CatalogosBdResponse(
                estadoLicitacionRepository.findAll(Sort.by("id")).stream().map(EstadoLicitacion::getNombre).toList(),
                estadoPostulacionRepository.findAll(Sort.by("id")).stream().map(EstadoPostulacion::getNombre).toList(),
                tipoArchivoRepository.findAll(Sort.by("id")).stream().map(TipoArchivo::getNombre).toList());
    }

    private void copiar(LicitacionBdRequest r, Licitacion l) {
        l.setLicitadorId(r.licitadorId());
        l.setTitulo(r.titulo().trim());
        l.setDescripcion(r.descripcion().trim());
        l.setRubroId(r.rubroId());
        l.setRegionId(r.regionId());
        l.setPresupuestoMin(r.presupuestoMin());
        l.setPresupuestoMax(r.presupuestoMax());
        l.setMaxPostulantes(r.maxPostulantes());
        l.setFechaCierre(r.fechaCierre());
    }

    private Map<Integer, Long> conteos(List<Licitacion> lista) {
        if (lista.isEmpty()) {
            return Map.of();
        }
        Map<Integer, Long> m = new HashMap<>();
        postulacionRepository.contarPorLicitacion(lista.stream().map(Licitacion::getId).toList())
                .forEach(fila -> m.put((Integer) fila[0], (Long) fila[1]));
        return m;
    }

    private Licitacion licitacion(Integer id) {
        return licitacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LICITACION_NO_ENCONTRADA", "La licitación no existe"));
    }

    private EstadoLicitacion estadoLicitacion(String nombre) {
        return estadoLicitacionRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new BadRequestException("ESTADO_INVALIDO", "Estado de licitación inválido: " + nombre));
    }
}
