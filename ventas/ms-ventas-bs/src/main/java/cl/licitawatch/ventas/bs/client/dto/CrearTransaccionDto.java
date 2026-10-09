package cl.licitawatch.ventas.bs.client.dto;

import java.math.BigDecimal;

public record CrearTransaccionDto(String ordenCompra, String sesionId, BigDecimal monto, String urlRetorno) {
}
