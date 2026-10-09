package cl.licitawatch.licitaciones.bs.service;

import cl.licitawatch.licitaciones.bs.client.dto.LicitacionBdDto;

import java.util.Collection;
import java.util.Map;

/** Resuelve nombres de las REF (rubro, región, licitador) contra MS.usuarios.bs. */
public interface CatalogoNombresService {
    record Nombres(Map<Integer, String> rubros, Map<Integer, String> regiones, Map<Integer, String> licitadores) {
    }

    Nombres para(Collection<LicitacionBdDto> licitaciones);

    void validarRubroYRegion(Integer rubroId, Integer regionId);
}
