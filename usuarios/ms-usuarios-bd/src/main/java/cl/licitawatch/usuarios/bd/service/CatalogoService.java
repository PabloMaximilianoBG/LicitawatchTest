package cl.licitawatch.usuarios.bd.service;

import cl.licitawatch.usuarios.bd.dto.request.RubroRequest;
import cl.licitawatch.usuarios.bd.dto.response.CatalogoResponse;
import cl.licitawatch.usuarios.bd.dto.response.CiudadResponse;

import java.util.List;

public interface CatalogoService {
    List<CatalogoResponse> roles();

    List<CatalogoResponse> rubros();

    CatalogoResponse rubro(Integer id);

    CatalogoResponse crearRubro(RubroRequest request);

    List<CatalogoResponse> regiones();

    CatalogoResponse region(Integer id);

    List<CiudadResponse> ciudadesDeRegion(Integer regionId);

    CiudadResponse ciudad(Integer id);

    List<CatalogoResponse> tamanosEmpresa();
}
