package cl.licitawatch.ventas.bd.dto.response;

import java.math.BigDecimal;

public record ResumenBdResponse(BigDecimal totalRecaudado, long pagosAprobados, long pagosRechazados, long ventasSinPago,
                                long premiumActivas, long estandarActivas) {
}
