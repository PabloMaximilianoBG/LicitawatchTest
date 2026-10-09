package cl.licitawatch.ventas.bff.dto.request;

/** Parámetros con los que Webpay vuelve a la plataforma (token_ws en el flujo normal; TBK_* si se anuló o expiró). */
public record RetornoWebpayRequest(String tokenWs, String tbkToken, String tbkOrdenCompra, String tbkIdSesion) {
}
