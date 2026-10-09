package cl.licitawatch.ventas.bs.client.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ResultadoPagoDto(boolean aprobado, String estado, Integer codigoRespuesta, BigDecimal monto, String ordenCompra,
                               String codigoAutorizacion, String tipoPago, String metodoPago, String ultimosDigitos,
                               String fechaTransaccion) {
}
