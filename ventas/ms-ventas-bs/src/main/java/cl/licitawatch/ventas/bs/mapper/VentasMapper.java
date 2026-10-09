package cl.licitawatch.ventas.bs.mapper;

import cl.licitawatch.ventas.bs.client.dto.SuscripcionBdDto;
import cl.licitawatch.ventas.bs.client.dto.UsuarioDto;
import cl.licitawatch.ventas.bs.client.dto.VentaBdDto;
import cl.licitawatch.ventas.bs.dto.response.SuscripcionResponse;
import cl.licitawatch.ventas.bs.dto.response.VentaResponse;
import cl.licitawatch.ventas.bs.util.Catalogos;
import org.springframework.stereotype.Component;

@Component
public class VentasMapper {

    public VentaResponse venta(VentaBdDto v, UsuarioDto cliente) {
        return VentaResponse.builder().id(v.id()).suscripcionId(v.suscripcionId()).usuarioId(v.usuarioId())
                .clienteNombre(cliente != null ? cliente.nombre() : null).clienteEmail(cliente != null ? cliente.email() : null)
                .plan(v.plan()).monto(v.monto()).fecha(v.fecha())
                .estadoPago(v.pago() != null ? v.pago().estado() : Catalogos.PENDIENTE)
                .metodoPago(v.pago() != null ? v.pago().metodo() : null)
                .idTransaccion(v.pago() != null ? v.pago().idTransaccion() : null).suscripcionEstado(v.suscripcionEstado()).build();
    }

    public SuscripcionResponse suscripcion(SuscripcionBdDto s, UsuarioDto cliente, int vigenciaDias) {
        boolean premium = Catalogos.PREMIUM.equals(s.plan());
        boolean pendiente = premium && Catalogos.CANCELADA.equals(s.estado()) && s.fechaUltimoPagoAprobado() == null && s.tieneVentaSinPago();
        return SuscripcionResponse.builder().id(s.id()).usuarioId(s.usuarioId())
                .clienteNombre(cliente != null ? cliente.nombre() : null).clienteEmail(cliente != null ? cliente.email() : null)
                .plan(s.plan()).estado(s.estado()).estadoVisible(pendiente ? "Pendiente de pago" : s.estado())
                .fechaInicio(s.fechaUltimoPagoAprobado())
                .fechaVencimiento(premium && s.fechaUltimoPagoAprobado() != null ? s.fechaUltimoPagoAprobado().plusDays(vigenciaDias) : null)
                .build();
    }
}
