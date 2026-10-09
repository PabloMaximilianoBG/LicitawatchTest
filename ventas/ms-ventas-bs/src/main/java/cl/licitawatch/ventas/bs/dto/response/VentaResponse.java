package cl.licitawatch.ventas.bs.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/** estadoPago = Aprobado | Rechazado | Pendiente (Pendiente = venta aún sin pago: el pago se crea al volver de Webpay). */
@Builder
public record VentaResponse(Integer id, Integer suscripcionId, Integer usuarioId, String clienteNombre, String clienteEmail,
                            String plan, BigDecimal monto, LocalDate fecha, String estadoPago, String metodoPago,
                            String idTransaccion, String suscripcionEstado) {
}
