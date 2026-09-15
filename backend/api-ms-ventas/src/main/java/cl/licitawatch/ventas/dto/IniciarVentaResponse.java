package cl.licitawatch.ventas.dto;

/**
 * "url" y "token" se usan para armar, en el frontend, el formulario HTML
 * auto-enviado (POST) que exige la integracion de Webpay Plus: no es un
 * simple redirect GET con query string, hay que hacer POST a "url" con un
 * input oculto token_ws=token.
 */
public record IniciarVentaResponse(
        Long ventaId,
        String token,
        String url
) {
}
