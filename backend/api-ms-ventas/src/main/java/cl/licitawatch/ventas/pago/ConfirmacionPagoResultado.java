package cl.licitawatch.ventas.pago;

import java.math.BigDecimal;

/**
 * "ultimosDigitosTarjeta" viene ya enmascarado por Transbank (nunca entrega
 * el numero completo, ni siquiera al comercio) - seguro de loguear y de
 * incluir en el correo de confirmacion. Puede ser null si la pasarela no lo
 * informa.
 */
public record ConfirmacionPagoResultado(
        boolean aprobado,
        String codigoAutorizacion,
        BigDecimal monto,
        String ultimosDigitosTarjeta
) {
}
