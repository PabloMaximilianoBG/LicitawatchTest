package cl.licitawatch.notificaciones.bs.service;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.notificaciones.bs.dto.request.CorreoEnlaceRequest;
import cl.licitawatch.notificaciones.bs.dto.request.NotificacionRequest;
import cl.licitawatch.notificaciones.bs.dto.request.SoporteRequest;
import cl.licitawatch.notificaciones.bs.dto.response.NotificacionResponse;
import cl.licitawatch.notificaciones.bs.dto.response.SoporteResponse;

/** PPT diap. 8: notificación automática por correo. Un registro en notificacion por cada evento del catálogo. */
public interface NotificacionService {
    NotificacionResponse notificar(NotificacionRequest request);

    void confirmacionCuenta(CorreoEnlaceRequest request);

    void restablecerPassword(CorreoEnlaceRequest request);

    SoporteResponse soporte(UsuarioActual usuario, SoporteRequest request);

    PaginaResponse<NotificacionResponse> mias(UsuarioActual usuario, int page, int size);

    PaginaResponse<NotificacionResponse> adminListar(UsuarioActual admin, String tipo, int page, int size);
}
