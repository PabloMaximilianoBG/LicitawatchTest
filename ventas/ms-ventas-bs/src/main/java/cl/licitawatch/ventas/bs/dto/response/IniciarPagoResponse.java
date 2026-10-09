package cl.licitawatch.ventas.bs.dto.response;

import java.math.BigDecimal;

/** El frontend envía token_ws por POST a url (formulario de Webpay). */
public record IniciarPagoResponse(Integer ventaId, BigDecimal monto, String token, String url) {
}
