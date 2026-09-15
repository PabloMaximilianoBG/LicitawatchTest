package cl.licitawatch.ventas.service;

import cl.licitawatch.ventas.dto.ConfirmarPagoRequest;
import cl.licitawatch.ventas.dto.IniciarVentaRequest;
import cl.licitawatch.ventas.dto.IniciarVentaResponse;
import cl.licitawatch.ventas.dto.NotificacionRequest;
import cl.licitawatch.ventas.dto.PagoResponse;
import cl.licitawatch.ventas.dto.VentaAdminResponse;
import cl.licitawatch.ventas.entity.EstadoPago;
import cl.licitawatch.ventas.entity.EstadoSuscripcion;
import cl.licitawatch.ventas.entity.Pago;
import cl.licitawatch.ventas.entity.PlanSuscripcion;
import cl.licitawatch.ventas.entity.Suscripcion;
import cl.licitawatch.ventas.entity.Venta;
import cl.licitawatch.ventas.exception.OperacionNoPermitidaException;
import cl.licitawatch.ventas.exception.PlanGratuitoException;
import cl.licitawatch.ventas.exception.RecursoNoEncontradoException;
import cl.licitawatch.ventas.pago.ConfirmacionPagoResultado;
import cl.licitawatch.ventas.pago.InicioPagoResultado;
import cl.licitawatch.ventas.pago.PasarelaPagoAdapter;
import cl.licitawatch.ventas.repository.PagoRepository;
import cl.licitawatch.ventas.repository.PlanSuscripcionRepository;
import cl.licitawatch.ventas.repository.SuscripcionRepository;
import cl.licitawatch.ventas.repository.VentaRepository;
import cl.licitawatch.ventas.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VentaService {

    private final PlanSuscripcionRepository planRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final VentaRepository ventaRepository;
    private final PagoRepository pagoRepository;
    private final PasarelaPagoAdapter pasarelaPagoAdapter;
    private final NotificacionClient notificacionClient;
    private final String returnUrl;

    public VentaService(
            PlanSuscripcionRepository planRepository,
            SuscripcionRepository suscripcionRepository,
            VentaRepository ventaRepository,
            PagoRepository pagoRepository,
            PasarelaPagoAdapter pasarelaPagoAdapter,
            NotificacionClient notificacionClient,
            @Value("${licitawatch.transbank.return-url}") String returnUrl) {
        this.planRepository = planRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.ventaRepository = ventaRepository;
        this.pagoRepository = pagoRepository;
        this.pasarelaPagoAdapter = pasarelaPagoAdapter;
        this.notificacionClient = notificacionClient;
        this.returnUrl = returnUrl;
    }

    @Transactional
    public IniciarVentaResponse iniciar(AuthenticatedUser usuario, IniciarVentaRequest req) {
        PlanSuscripcion plan = planRepository.findById(req.planId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan no encontrado"));

        if (plan.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PlanGratuitoException();
        }

        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setUsuarioId(usuario.id());
        suscripcion.setPlan(plan);
        suscripcion.setEstado(EstadoSuscripcion.VENCIDA);
        suscripcionRepository.save(suscripcion);

        Venta venta = new Venta();
        venta.setSuscripcion(suscripcion);
        venta.setMonto(plan.getPrecio());
        ventaRepository.save(venta);

        String buyOrder = "V" + venta.getId();
        String sessionId = "U" + usuario.id() + "-" + venta.getId();
        InicioPagoResultado resultado = pasarelaPagoAdapter.iniciar(buyOrder, sessionId, plan.getPrecio(), returnUrl);

        Pago pago = new Pago();
        pago.setVenta(venta);
        pago.setToken(resultado.token());
        pago.setEstado(EstadoPago.PENDIENTE);
        pagoRepository.save(pago);

        return new IniciarVentaResponse(venta.getId(), resultado.token(), resultado.url());
    }

    @Transactional
    public PagoResponse confirmar(AuthenticatedUser usuario, ConfirmarPagoRequest req) {
        Pago pago = pagoRepository.findByTokenParaActualizar(req.tokenWs())
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado"));
        Suscripcion suscripcion = pago.getVenta().getSuscripcion();

        if (!suscripcion.getUsuarioId().equals(usuario.id())) {
            throw new OperacionNoPermitidaException("Este pago no te pertenece");
        }

        if (pago.getEstado() == EstadoPago.PENDIENTE) {
            ConfirmacionPagoResultado resultado = pasarelaPagoAdapter.confirmar(req.tokenWs());
            pago.setIdTransaccion(resultado.codigoAutorizacion());
            pago.setEstado(resultado.aprobado() ? EstadoPago.APROBADO : EstadoPago.RECHAZADO);
            pagoRepository.save(pago);

            if (resultado.aprobado()) {
                suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
                suscripcion.setFechaInicio(LocalDate.now());
                suscripcion.setFechaVencimiento(LocalDate.now().plusMonths(1));
                suscripcionRepository.save(suscripcion);
            }

            notificacionClient.notificar(new NotificacionRequest(
                    suscripcion.getUsuarioId(), "PAGO", "EMAIL",
                    resultado.aprobado() ? "Confirmacion de pago - LicitaWatch" : "Pago rechazado - LicitaWatch",
                    construirMensajeConfirmacion(suscripcion, pago, resultado)));
        }

        return new PagoResponse(pago.getEstado().name(), suscripcion.getEstado().name(),
                suscripcion.getPlan().getNombre().name(), suscripcion.getFechaVencimiento());
    }

    /**
     * Nunca se incluye el numero de tarjeta completo: "ultimosDigitosTarjeta"
     * ya viene enmascarado por Transbank (seccion 5.3 del diseno: ningun
     * dato de tarjeta pasa por nuestro propio backend).
     */
    private String construirMensajeConfirmacion(Suscripcion suscripcion, Pago pago, ConfirmacionPagoResultado resultado) {
        if (!resultado.aprobado()) {
            return "Hola,\n\n"
                    + "Tu intento de pago del plan " + suscripcion.getPlan().getNombre() + " (" + formatoMonto(suscripcion.getPlan().getPrecio()) + ") fue rechazado por la pasarela de pago.\n"
                    + "Puedes intentarlo nuevamente desde la seccion Planes de LicitaWatch.\n\n"
                    + "Equipo LicitaWatch";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Hola,\n\n")
                .append("Confirmamos la contratacion de tu plan en LicitaWatch. Este es el detalle de tu compra:\n\n")
                .append("Plan contratado: ").append(suscripcion.getPlan().getNombre()).append("\n")
                .append("Monto pagado: ").append(formatoMonto(resultado.monto())).append("\n")
                .append("Vigente desde: ").append(suscripcion.getFechaInicio()).append("\n")
                .append("Vigente hasta: ").append(suscripcion.getFechaVencimiento()).append("\n")
                .append("Codigo de autorizacion Webpay: ").append(resultado.codigoAutorizacion()).append("\n");
        if (resultado.ultimosDigitosTarjeta() != null) {
            sb.append("Tarjeta: terminada en ").append(resultado.ultimosDigitosTarjeta()).append("\n");
        }
        sb.append("\nTu plan ya esta activo. Gracias por confiar en LicitaWatch.\n\n")
                .append("Equipo LicitaWatch");
        return sb.toString();
    }

    private String formatoMonto(java.math.BigDecimal monto) {
        return "$" + monto.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    @Transactional(readOnly = true)
    public List<VentaAdminResponse> listarTodas() {
        return ventaRepository.findAllByOrderByFechaDesc().stream()
                .map(v -> new VentaAdminResponse(
                        v.getId(),
                        v.getSuscripcion().getUsuarioId(),
                        v.getSuscripcion().getPlan().getNombre().name(),
                        v.getMonto(),
                        v.getFecha(),
                        pagoRepository.findByVenta_Id(v.getId())
                                .map(p -> p.getEstado().name())
                                .orElse("SIN_PAGO")))
                .toList();
    }
}
