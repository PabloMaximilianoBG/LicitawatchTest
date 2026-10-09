package cl.licitawatch.ventas.bff.service;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.ventas.bff.dto.request.RetornoWebpayRequest;
import cl.licitawatch.ventas.bff.dto.response.*;

import java.util.List;

public interface VentasBffService {
    List<PlanResponse> planes();

    MiSuscripcionResponse miSuscripcion();

    IniciarPagoResponse iniciarPremium();

    List<VentaResponse> misVentas();

    VentaResponse venta(Integer id);

    /** Devuelve la URL del frontend a la que se redirige al usuario tras Webpay. */
    String retornoWebpay(RetornoWebpayRequest request);

    PaginaResponse<VentaResponse> adminVentas(int page, int size);

    ResumenVentasResponse resumen();

    PaginaResponse<SuscripcionResponse> adminSuscripciones(String estado, String plan, int page, int size);

    SuscripcionResponse cancelar(Integer id);
}
