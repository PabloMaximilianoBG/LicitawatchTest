package cl.licitawatch.licitaciones.bd.service;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.licitaciones.bd.dto.request.PostulacionBdRequest;
import cl.licitawatch.licitaciones.bd.dto.response.AdjudicacionBdResponse;
import cl.licitawatch.licitaciones.bd.dto.response.PostulacionBdResponse;

import java.time.LocalDate;
import java.util.List;

public interface PostulacionDataService {
    PaginaResponse<PostulacionBdResponse> listar(Integer licitacionId, Integer pymeId, String estado, int page, int size);

    List<PostulacionBdResponse> deLicitacion(Integer licitacionId);

    PostulacionBdResponse obtener(Integer id);

    PostulacionBdResponse crear(PostulacionBdRequest request);

    PostulacionBdResponse cambiarEstado(Integer id, String estado);

    long contarDesde(Integer pymeId, LocalDate desde);

    boolean existe(Integer licitacionId, Integer pymeId);

    /** En UNA transacción: la postulación pasa a Aprobada, las demás Pendientes a Rechazada y la licitación a Adjudicada. */
    AdjudicacionBdResponse adjudicar(Integer licitacionId, Integer postulacionId);
}
