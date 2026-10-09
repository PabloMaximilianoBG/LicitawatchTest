package cl.licitawatch.ventas.bs.service;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.ventas.bs.dto.request.RetornoWebpayRequest;
import cl.licitawatch.ventas.bs.dto.response.IniciarPagoResponse;
import cl.licitawatch.ventas.bs.dto.response.ResumenVentasResponse;
import cl.licitawatch.ventas.bs.dto.response.RetornoResponse;
import cl.licitawatch.ventas.bs.dto.response.VentaResponse;

import java.util.List;

/** PPT diap. 7 y 15: cobro del plan Premium a la Pyme (plan -> suscripción -> venta -> pago) vía MS.ventas.ambassador. */
public interface PagoService {
    IniciarPagoResponse iniciarPremium(UsuarioActual pyme);

    RetornoResponse procesarRetorno(RetornoWebpayRequest request);

    List<VentaResponse> misVentas(UsuarioActual pyme);

    VentaResponse venta(UsuarioActual usuario, Integer ventaId);

    PaginaResponse<VentaResponse> adminVentas(UsuarioActual admin, int page, int size);

    ResumenVentasResponse resumen(UsuarioActual admin);
}
