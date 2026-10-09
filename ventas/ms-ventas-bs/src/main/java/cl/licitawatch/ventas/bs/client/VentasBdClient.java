package cl.licitawatch.ventas.bs.client;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.ventas.bs.client.dto.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

import java.util.List;

/** Contrato REST de MS.ventas.bd. */
@HttpExchange("/bd")
public interface VentasBdClient {

    @GetExchange("/planes")
    List<CatalogoDto> planes();

    @GetExchange("/suscripciones")
    PaginaResponse<SuscripcionBdDto> suscripciones(@RequestParam(required = false) Integer usuarioId,
                                                   @RequestParam(required = false) String estado,
                                                   @RequestParam(required = false) String plan,
                                                   @RequestParam int page, @RequestParam int size);

    @GetExchange("/suscripciones/usuario/{usuarioId}")
    List<SuscripcionBdDto> suscripcionesDeUsuario(@PathVariable Integer usuarioId);

    @GetExchange("/suscripciones/{id}")
    SuscripcionBdDto suscripcion(@PathVariable Integer id);

    @PostExchange("/suscripciones")
    SuscripcionBdDto crearSuscripcion(@RequestBody SuscripcionBdRequestDto request);

    @PatchExchange("/suscripciones/{id}/estado")
    SuscripcionBdDto cambiarEstadoSuscripcion(@PathVariable Integer id, @RequestBody EstadoDto request);

    @GetExchange("/suscripciones/usuarios-premium")
    List<Integer> usuariosPremium(@RequestParam List<Integer> usuarioIds);

    @GetExchange("/suscripciones/premium-activas")
    List<SuscripcionBdDto> premiumActivas();

    @PostExchange("/ventas")
    VentaBdDto crearVenta(@RequestBody VentaBdRequestDto request);

    @GetExchange("/ventas/{id}")
    VentaBdDto venta(@PathVariable Integer id);

    @GetExchange("/ventas")
    PaginaResponse<VentaBdDto> ventas(@RequestParam(required = false) Integer usuarioId, @RequestParam int page, @RequestParam int size);

    @GetExchange("/ventas/pendientes-premium")
    List<VentaBdDto> ventasPendientesPremium(@RequestParam Integer usuarioId);

    @PostExchange("/ventas/{id}/pago")
    VentaBdDto registrarPago(@PathVariable Integer id, @RequestBody PagoBdRequestDto request);

    @GetExchange("/resumen")
    ResumenBdDto resumen();
}
