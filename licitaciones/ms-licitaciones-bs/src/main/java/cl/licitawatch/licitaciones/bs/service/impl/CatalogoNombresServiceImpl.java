package cl.licitawatch.licitaciones.bs.service.impl;

import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.common.exception.RemoteServiceException;
import cl.licitawatch.licitaciones.bs.client.UsuarioClient;
import cl.licitawatch.licitaciones.bs.client.dto.CatalogoDto;
import cl.licitawatch.licitaciones.bs.client.dto.LicitacionBdDto;
import cl.licitawatch.licitaciones.bs.client.dto.PerfilDto;
import cl.licitawatch.licitaciones.bs.service.CatalogoNombresService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogoNombresServiceImpl implements CatalogoNombresService {
    private final UsuarioClient usuarios;

    @Override
    public Nombres para(Collection<LicitacionBdDto> licitaciones) {
        if (licitaciones.isEmpty()) {
            return new Nombres(Map.of(), Map.of(), Map.of());
        }
        Map<Integer, String> rubros = usuarios.rubros().stream().collect(Collectors.toMap(CatalogoDto::id, CatalogoDto::nombre));
        Map<Integer, String> regiones = usuarios.regiones().stream().collect(Collectors.toMap(CatalogoDto::id, CatalogoDto::nombre));
        List<Integer> ids = licitaciones.stream().map(LicitacionBdDto::licitadorId).distinct().toList();
        Map<Integer, String> licitadores = new HashMap<>();
        try {
            for (PerfilDto p : usuarios.licitadores(ids)) {
                licitadores.put(p.perfilId(), p.razonSocial());
            }
        } catch (Exception e) {
            log.warn("No se pudieron obtener los nombres de los licitadores: {}", e.getMessage());
        }
        return new Nombres(rubros, regiones, licitadores);
    }

    /** rubro_id y region_id son REF a usuarios-bd: se validan por REST (no hay FK entre bases). */
    @Override
    public void validarRubroYRegion(Integer rubroId, Integer regionId) {
        try {
            usuarios.rubro(rubroId);
        } catch (RemoteServiceException e) {
            if (e.esNoEncontrado()) {
                throw new BadRequestException("RUBRO_INVALIDO", "El rubro seleccionado no existe");
            }
            throw e;
        }
        try {
            usuarios.region(regionId);
        } catch (RemoteServiceException e) {
            if (e.esNoEncontrado()) {
                throw new BadRequestException("REGION_INVALIDA", "La región seleccionada no existe");
            }
            throw e;
        }
    }
}
