package cl.licitawatch.ventas.bff.client;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.ventas.bff.dto.request.RetornoWebpayRequest;
import cl.licitawatch.ventas.bff.dto.response.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

import java.util.List;

@HttpExchange("/bs")
public interface VentasBsClient {
    @GetExchange("/planes")
    List<PlanResponse> planes();

    @GetExchange("/suscripciones/mi")
    MiSuscripcionResponse miSuscripcion();

    @PostExchange("/ventas/premium")
    IniciarPagoResponse iniciarPremium();

    @GetExchange("/ventas/mias")
    List<VentaResponse> misVentas();

    @GetExchange("/ventas/{id}")
    VentaResponse venta(@PathVariable Integer id);

    @PostExchange("/pagos/webpay/retorno")
    RetornoResponse retorno(@RequestBody RetornoWebpayRequest request);

    @GetExchange("/admin/ventas")
    PaginaResponse<VentaResponse> adminVentas(@RequestParam int page, @RequestParam int size);

    @GetExchange("/admin/ventas/resumen")
    ResumenVentasResponse resumen();

    @GetExchange("/admin/suscripciones")
    PaginaResponse<SuscripcionResponse> adminSuscripciones(@RequestParam(required = false) String estado,
                                                           @RequestParam(required = false) String plan,
                                                           @RequestParam int page, @RequestParam int size);

    @PatchExchange("/admin/suscripciones/{id}/cancelar")
    SuscripcionResponse cancelar(@PathVariable Integer id);
}
