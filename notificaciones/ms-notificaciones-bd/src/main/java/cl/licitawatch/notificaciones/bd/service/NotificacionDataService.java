package cl.licitawatch.notificaciones.bd.service;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.notificaciones.bd.dto.request.NotificacionBdRequest;
import cl.licitawatch.notificaciones.bd.dto.response.CatalogoResponse;
import cl.licitawatch.notificaciones.bd.dto.response.NotificacionBdResponse;

import java.util.List;

public interface NotificacionDataService {
    NotificacionBdResponse registrar(NotificacionBdRequest request);

    PaginaResponse<NotificacionBdResponse> listar(Integer usuarioId, String tipo, int page, int size);

    List<CatalogoResponse> tipos();
}
