package cl.licitawatch.ventas.bs.service.impl;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.exception.ConflictException;
import cl.licitawatch.common.exception.ForbiddenException;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.ventas.bs.client.NotificacionClient;
import cl.licitawatch.ventas.bs.client.PasarelaClient;
import cl.licitawatch.ventas.bs.client.VentasBdClient;
import cl.licitawatch.ventas.bs.client.dto.*;
import cl.licitawatch.ventas.bs.config.PlanesProperties;
import cl.licitawatch.ventas.bs.dto.request.RetornoWebpayRequest;
import cl.licitawatch.ventas.bs.dto.response.*;
import cl.licitawatch.ventas.bs.mapper.VentasMapper;
import cl.licitawatch.ventas.bs.service.ClienteService;
import cl.licitawatch.ventas.bs.service.PagoService;
import cl.licitawatch.ventas.bs.service.SuscripcionService;
import cl.licitawatch.ventas.bs.util.Catalogos;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {
    private static final Pattern ORDEN = Pattern.compile("^LW-V(\\d+)-\\d+$");
    private static final DateTimeFormatter SELLO = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private final VentasBdClient bd;
    private final PasarelaClient pasarela;
    private final NotificacionClient notificaciones;
    private final SuscripcionService suscripciones;
    private final ClienteService clienteService;
    private final PlanesProperties planes;
    private final VentasMapper mapper;

    @Value("${licitawatch.frontend-url:http://localhost:5173}")
    private String frontendUrl;
    @Value("${licitawatch.webpay.return-url}")
    private String returnUrl;

    @Override
    public IniciarPagoResponse iniciarPremium(UsuarioActual u) {
        SuscripcionServiceImpl.exigirPyme(u);
        if (suscripciones.planVigente(u.usuarioId()).premium()) {
            throw new ConflictException("PLAN_VIGENTE", "Ya tienes el plan Premium vigente");
        }
        // Si hay una compra sin pagar se reutiliza (queda "Pendiente" hasta volver de Webpay); si no, se crea la cadena
        // plan -> suscripción (Cancelada hasta que el pago se apruebe) -> venta (monto del día).
        VentaBdDto venta = bd.ventasPendientesPremium(u.usuarioId()).stream().findFirst().orElseGet(() -> {
            SuscripcionBdDto s = bd.crearSuscripcion(new SuscripcionBdRequestDto(u.usuarioId(), Catalogos.PREMIUM, Catalogos.CANCELADA));
            return bd.crearVenta(new VentaBdRequestDto(s.id(), planes.premium().precio(), Catalogos.hoy()));
        });
        String orden = "LW-V" + venta.id() + "-" + LocalDateTime.now(Catalogos.CHILE).format(SELLO);
        TransaccionDto t = pasarela.crear(new CrearTransaccionDto(orden, "U" + u.usuarioId(), venta.monto(), returnUrl));
        return new IniciarPagoResponse(venta.id(), venta.monto(), t.token(), t.url());
    }

    @Override
    public RetornoResponse procesarRetorno(RetornoWebpayRequest r) {
        if (r.tokenWs() == null || r.tokenWs().isBlank() || (r.tbkToken() != null && !r.tbkToken().isBlank())) {
            // Pago anulado por el usuario o tiempo agotado: la venta queda pendiente (se puede reintentar).
            Integer ventaId = ventaDeOrden(r.tbkOrdenCompra());
            return redireccion(ventaId, "anulado");
        }
        ResultadoPagoDto resultado;
        try {
            resultado = pasarela.confirmar(r.tokenWs());
        } catch (Exception e) {
            log.warn("Commit Webpay falló ({}); se consulta el estado de la transacción", e.getMessage());
            try {
                resultado = pasarela.estado(r.tokenWs());
            } catch (Exception e2) {
                log.error("No se pudo confirmar ni consultar la transacción Webpay: {}", e2.getMessage());
                return redireccion(null, "error");
            }
        }
        Integer ventaId = ventaDeOrden(resultado.ordenCompra());
        if (ventaId == null) {
            return redireccion(null, "error");
        }
        VentaBdDto venta = bd.venta(ventaId);
        if (venta.pago() != null) {
            return redireccion(ventaId, Catalogos.APROBADO.equals(venta.pago().estado()) ? "aprobado" : "rechazado");
        }
        boolean montoCorrecto = resultado.monto() != null && resultado.monto().compareTo(venta.monto()) == 0;
        String metodo = resultado.metodoPago() != null ? resultado.metodoPago() : "Crédito";
        if (resultado.aprobado() && montoCorrecto) {
            VentaBdDto pagada = bd.registrarPago(ventaId, new PagoBdRequestDto(r.tokenWs(), metodo, Catalogos.APROBADO, true));
            notificarPago(pagada, resultado);
            return redireccion(ventaId, "aprobado");
        }
        bd.registrarPago(ventaId, new PagoBdRequestDto(r.tokenWs(), metodo, Catalogos.RECHAZADO, false));
        return redireccion(ventaId, "rechazado");
    }

    @Override
    public List<VentaResponse> misVentas(UsuarioActual u) {
        SuscripcionServiceImpl.exigirPyme(u);
        return bd.ventas(u.usuarioId(), 0, 100).content().stream().map(v -> mapper.venta(v, null)).toList();
    }

    @Override
    public VentaResponse venta(UsuarioActual u, Integer ventaId) {
        VentaBdDto v = bd.venta(ventaId);
        if (!u.esAdministrador() && !Objects.equals(v.usuarioId(), u.usuarioId())) {
            throw new ForbiddenException("La venta no te pertenece");
        }
        return mapper.venta(v, null);
    }

    @Override
    public PaginaResponse<VentaResponse> adminVentas(UsuarioActual admin, int page, int size) {
        SuscripcionServiceImpl.exigirAdmin(admin);
        PaginaResponse<VentaBdDto> pagina = bd.ventas(null, page, size);
        Map<Integer, UsuarioDto> clientes = clienteService.clientes(pagina.content().stream().map(VentaBdDto::usuarioId).toList());
        return pagina.map(v -> mapper.venta(v, clientes.get(v.usuarioId())));
    }

    @Override
    public ResumenVentasResponse resumen(UsuarioActual admin) {
        SuscripcionServiceImpl.exigirAdmin(admin);
        ResumenBdDto r = bd.resumen();
        return new ResumenVentasResponse(r.totalRecaudado(), r.pagosAprobados(), r.pagosRechazados(), r.ventasSinPago(),
                r.premiumActivas(), r.estandarActivas());
    }

    private void notificarPago(VentaBdDto venta, ResultadoPagoDto resultado) {
        try {
            NumberFormat clp = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"));
            clp.setMaximumFractionDigits(0);
            Map<String, String> datos = new LinkedHashMap<>();
            datos.put("plan", venta.plan());
            datos.put("monto", clp.format(venta.monto()));
            datos.put("fecha", String.valueOf(venta.fecha()));
            datos.put("ordenCompra", resultado.ordenCompra());
            datos.put("metodoPago", resultado.metodoPago());
            datos.put("codigoAutorizacion", resultado.codigoAutorizacion());
            if (resultado.ultimosDigitos() != null) {
                datos.put("tarjeta", "**** " + resultado.ultimosDigitos());
            }
            datos.put("vigenteHasta", String.valueOf(venta.fecha().plusDays(planes.premium().vigenciaDias())));
            notificaciones.notificar(new NotificacionDto(venta.usuarioId(), Catalogos.NOTIF_PAGO_CONFIRMADO, datos));
        } catch (Exception e) {
            log.error("No se pudo notificar el pago de la venta {}: {}", venta.id(), e.getMessage());
        }
    }

    private RetornoResponse redireccion(Integer ventaId, String estado) {
        return new RetornoResponse(frontendUrl + "/pago/resultado?estado=" + estado + (ventaId != null ? "&venta=" + ventaId : ""));
    }

    static Integer ventaDeOrden(String orden) {
        if (orden == null) {
            return null;
        }
        Matcher m = ORDEN.matcher(orden);
        return m.matches() ? Integer.valueOf(m.group(1)) : null;
    }
}
