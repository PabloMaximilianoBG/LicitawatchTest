package cl.licitawatch.ventas.bs.client.dto;

import java.math.BigDecimal;

public record ResumenBdDto(BigDecimal totalRecaudado, long pagosAprobados, long pagosRechazados, long ventasSinPago,
                           long premiumActivas, long estandarActivas) {
}
