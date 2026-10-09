package cl.licitawatch.usuarios.bd.service.impl;

import cl.licitawatch.common.exception.ConflictException;
import cl.licitawatch.common.exception.ResourceNotFoundException;
import cl.licitawatch.usuarios.bd.dto.request.RubroRequest;
import cl.licitawatch.usuarios.bd.dto.response.CatalogoResponse;
import cl.licitawatch.usuarios.bd.dto.response.CiudadResponse;
import cl.licitawatch.usuarios.bd.entity.Rubro;
import cl.licitawatch.usuarios.bd.mapper.UsuarioBdMapper;
import cl.licitawatch.usuarios.bd.repository.*;
import cl.licitawatch.usuarios.bd.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogoServiceImpl implements CatalogoService {
    private final RolRepository rolRepository;
    private final RubroRepository rubroRepository;
    private final RegionRepository regionRepository;
    private final CiudadRepository ciudadRepository;
    private final TamanoEmpresaRepository tamanoRepository;
    private final UsuarioBdMapper mapper;

    @Override
    public List<CatalogoResponse> roles() {
        return rolRepository.findAll(Sort.by("id")).stream().map(mapper::rol).toList();
    }

    @Override
    public List<CatalogoResponse> rubros() {
        return rubroRepository.findAll(Sort.by("nombre")).stream().map(mapper::rubro).toList();
    }

    @Override
    public CatalogoResponse rubro(Integer id) {
        return rubroRepository.findById(id).map(mapper::rubro)
                .orElseThrow(() -> new ResourceNotFoundException("RUBRO_NO_ENCONTRADO", "El rubro no existe"));
    }

    @Override
    @Transactional
    public CatalogoResponse crearRubro(RubroRequest request) {
        String nombre = request.nombre().trim();
        if (rubroRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictException("RUBRO_DUPLICADO", "El rubro ya existe");
        }
        return mapper.rubro(rubroRepository.save(Rubro.builder().nombre(nombre).build()));
    }

    @Override
    public List<CatalogoResponse> regiones() {
        return regionRepository.findAll(Sort.by("id")).stream().map(mapper::region).toList();
    }

    @Override
    public CatalogoResponse region(Integer id) {
        return regionRepository.findById(id).map(mapper::region)
                .orElseThrow(() -> new ResourceNotFoundException("REGION_NO_ENCONTRADA", "La región no existe"));
    }

    @Override
    public List<CiudadResponse> ciudadesDeRegion(Integer regionId) {
        return ciudadRepository.findByRegionIdOrderByNombreAsc(regionId).stream().map(mapper::ciudad).toList();
    }

    @Override
    public CiudadResponse ciudad(Integer id) {
        return ciudadRepository.findById(id).map(mapper::ciudad)
                .orElseThrow(() -> new ResourceNotFoundException("CIUDAD_NO_ENCONTRADA", "La ciudad no existe"));
    }

    @Override
    public List<CatalogoResponse> tamanosEmpresa() {
        return tamanoRepository.findAll(Sort.by("id")).stream().map(mapper::tamano).toList();
    }
}
