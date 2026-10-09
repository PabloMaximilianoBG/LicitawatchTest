package cl.licitawatch.ventas.bd.mapper;

import cl.licitawatch.ventas.bd.dto.response.PagoBdResponse;
import cl.licitawatch.ventas.bd.dto.response.VentaBdResponse;
import cl.licitawatch.ventas.bd.entity.Pago;
import cl.licitawatch.ventas.bd.entity.Venta;
import org.springframework.stereotype.Component;

@Component
public class VentasBdMapper {

    public VentaBdResponse venta(Venta v, Pago p) {
        return VentaBdResponse.builder().id(v.getId()).suscripcionId(v.getSuscripcion().getId())
                .usuarioId(v.getSuscripcion().getUsuarioId()).plan(v.getSuscripcion().getPlan().getNombre())
                .suscripcionEstado(v.getSuscripcion().getEstadoSuscripcion().getNombre()).monto(v.getMonto()).fecha(v.getFecha())
                .pago(p == null ? null : new PagoBdResponse(p.getId(), p.getIdTransaccion(), p.getMetodoPago().getNombre(),
                        p.getEstadoPago().getNombre()))
                .build();
    }
}
