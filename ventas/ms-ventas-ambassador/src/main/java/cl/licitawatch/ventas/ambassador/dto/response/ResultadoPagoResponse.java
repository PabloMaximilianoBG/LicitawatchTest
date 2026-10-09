package cl.licitawatch.ventas.ambassador.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

/** Resultado normalizado de la pasarela. metodoPago ya viene traducido al catálogo metodo_pago (Crédito / Débito). */
@Builder
public record ResultadoPagoResponse(boolean aprobado, String estado, Integer codigoRespuesta, BigDecimal monto, String ordenCompra,
                                    String codigoAutorizacion, String tipoPago, String metodoPago, String ultimosDigitos,
                                    String fechaTransaccion) {
}
