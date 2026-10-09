package cl.licitawatch.usuarios.bs.service;

import cl.licitawatch.usuarios.bs.dto.request.RubroRequest;
import cl.licitawatch.usuarios.bs.dto.response.CatalogoResponse;
import cl.licitawatch.usuarios.bs.dto.response.CiudadResponse;

import java.util.List;

public interface CatalogoService {
    List<CatalogoResponse> rubros();

    CatalogoResponse rubro(Integer id);

    CatalogoResponse crearRubro(RubroRequest request);

    List<CatalogoResponse> regiones();

    CatalogoResponse region(Integer id);

    List<CiudadResponse> ciudades(Integer regionId);

    List<CatalogoResponse> tamanosEmpresa();
}
