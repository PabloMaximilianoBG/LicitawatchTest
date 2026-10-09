package cl.licitawatch.ventas.bd.service;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.ventas.bd.dto.request.PagoBdRequest;
import cl.licitawatch.ventas.bd.dto.request.SuscripcionBdRequest;
import cl.licitawatch.ventas.bd.dto.request.VentaBdRequest;
import cl.licitawatch.ventas.bd.dto.response.*;

import java.util.List;

public interface VentasDataService {
    List<CatalogoResponse> planes();

    PaginaResponse<SuscripcionBdResponse> suscripciones(Integer usuarioId, String estado, String plan, int page, int size);

    List<SuscripcionBdResponse> suscripcionesDeUsuario(Integer usuarioId);

    SuscripcionBdResponse suscripcion(Integer id);

    SuscripcionBdResponse crearSuscripcion(SuscripcionBdRequest request);

    SuscripcionBdResponse cambiarEstadoSuscripcion(Integer id, String estado);

    List<Integer> usuariosPremium(List<Integer> usuarioIds);

    List<SuscripcionBdResponse> premiumActivas();

    VentaBdResponse crearVenta(VentaBdRequest request);

    VentaBdResponse venta(Integer id);

    PaginaResponse<VentaBdResponse> ventas(Integer usuarioId, int page, int size);

    List<VentaBdResponse> ventasPendientesPremium(Integer usuarioId);

    VentaBdResponse registrarPago(Integer ventaId, PagoBdRequest request);

    ResumenBdResponse resumen();
}
