package cl.licitawatch.notificaciones.bff.service.impl;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.notificaciones.bff.client.NotificacionBsClient;
import cl.licitawatch.notificaciones.bff.dto.request.SoporteRequest;
import cl.licitawatch.notificaciones.bff.dto.response.NotificacionResponse;
import cl.licitawatch.notificaciones.bff.dto.response.SoporteResponse;
import cl.licitawatch.notificaciones.bff.service.NotificacionBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificacionBffServiceImpl implements NotificacionBffService {
    private final NotificacionBsClient bs;

    @Override
    public PaginaResponse<NotificacionResponse> mias(int page, int size) {
        return bs.mias(page, size);
    }

    @Override
    public SoporteResponse soporte(SoporteRequest request) {
        return bs.soporte(request);
    }

    @Override
    public PaginaResponse<NotificacionResponse> admin(String tipo, int page, int size) {
        return bs.admin(tipo, page, size);
    }
}
