package cl.licitawatch.usuarios.bs.service.impl;

import cl.licitawatch.usuarios.bs.client.UsuarioBdClient;
import cl.licitawatch.usuarios.bs.client.dto.NombreDto;
import cl.licitawatch.usuarios.bs.dto.request.RubroRequest;
import cl.licitawatch.usuarios.bs.dto.response.CatalogoResponse;
import cl.licitawatch.usuarios.bs.dto.response.CiudadResponse;
import cl.licitawatch.usuarios.bs.mapper.PerfilMapper;
import cl.licitawatch.usuarios.bs.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Catálogos (rubro, región, ciudad, tamaño) para los combobox y para validar las REF de otros microservicios. */
@Service
@RequiredArgsConstructor
public class CatalogoServiceImpl implements CatalogoService {
    private final UsuarioBdClient bd;
    private final PerfilMapper mapper;

    @Override
    public List<CatalogoResponse> rubros() {
        return bd.rubros().stream().map(mapper::catalogo).toList();
    }

    @Override
    public CatalogoResponse rubro(Integer id) {
        return mapper.catalogo(bd.rubro(id));
    }

    @Override
    public CatalogoResponse crearRubro(RubroRequest request) {
        return mapper.catalogo(bd.crearRubro(new NombreDto(request.nombre().trim())));
    }

    @Override
    public List<CatalogoResponse> regiones() {
        return bd.regiones().stream().map(mapper::catalogo).toList();
    }

    @Override
    public CatalogoResponse region(Integer id) {
        return mapper.catalogo(bd.region(id));
    }

    @Override
    public List<CiudadResponse> ciudades(Integer regionId) {
        return bd.ciudades(regionId).stream().map(mapper::ciudad).toList();
    }

    @Override
    public List<CatalogoResponse> tamanosEmpresa() {
        return bd.tamanosEmpresa().stream().map(mapper::catalogo).toList();
    }
}
