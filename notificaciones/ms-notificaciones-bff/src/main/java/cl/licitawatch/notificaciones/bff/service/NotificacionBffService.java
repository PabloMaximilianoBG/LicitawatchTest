package cl.licitawatch.notificaciones.bff.service;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.notificaciones.bff.dto.request.SoporteRequest;
import cl.licitawatch.notificaciones.bff.dto.response.NotificacionResponse;
import cl.licitawatch.notificaciones.bff.dto.response.SoporteResponse;

public interface NotificacionBffService {
    PaginaResponse<NotificacionResponse> mias(int page, int size);

    SoporteResponse soporte(SoporteRequest request);

    PaginaResponse<NotificacionResponse> admin(String tipo, int page, int size);
}
