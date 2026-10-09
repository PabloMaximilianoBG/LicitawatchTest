package cl.licitawatch.ventas.bs.dto.response;

import java.math.BigDecimal;

public record ResumenVentasResponse(BigDecimal totalRecaudado, long pagosAprobados, long pagosRechazados, long ventasPendientes,
                                    long premiumActivas, long estandarActivas) {
}
