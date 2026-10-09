package cl.licitawatch.licitaciones.bd.service;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.licitaciones.bd.dto.request.ArchivosBdRequest;
import cl.licitawatch.licitaciones.bd.dto.request.LicitacionBdRequest;
import cl.licitawatch.licitaciones.bd.dto.response.CatalogosBdResponse;
import cl.licitawatch.licitaciones.bd.dto.response.EliminacionBdResponse;
import cl.licitawatch.licitaciones.bd.dto.response.LicitacionBdResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LicitacionDataService {
    record Filtros(String q, Integer rubroId, Integer regionId, String estado, Integer licitadorId, BigDecimal presupuestoMin,
                   BigDecimal presupuestoMax, LocalDate vigenteDesde, Boolean conCupo, String orden, int page, int size) {
    }

    PaginaResponse<LicitacionBdResponse> buscar(Filtros filtros);

    LicitacionBdResponse obtener(Integer id);

    List<LicitacionBdResponse> porIds(List<Integer> ids);

    List<LicitacionBdResponse> vencidas(LocalDate fecha);

    LicitacionBdResponse crear(LicitacionBdRequest request);

    LicitacionBdResponse actualizar(Integer id, LicitacionBdRequest request);

    LicitacionBdResponse cambiarEstado(Integer id, String estado);

    LicitacionBdResponse actualizarArchivos(Integer id, ArchivosBdRequest request);

    EliminacionBdResponse eliminar(Integer id);

    CatalogosBdResponse catalogos();
}
