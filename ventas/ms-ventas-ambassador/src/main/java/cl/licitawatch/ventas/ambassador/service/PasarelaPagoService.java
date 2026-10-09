package cl.licitawatch.ventas.ambassador.service;

import cl.licitawatch.ventas.ambassador.dto.request.CrearTransaccionRequest;
import cl.licitawatch.ventas.ambassador.dto.response.ResultadoPagoResponse;
import cl.licitawatch.ventas.ambassador.dto.response.TransaccionResponse;

/** Puerto de la pasarela de pago. Implementación actual: Webpay Plus (Transbank) en sandbox. */
public interface PasarelaPagoService {
    TransaccionResponse crearTransaccion(CrearTransaccionRequest request);

    ResultadoPagoResponse confirmar(String token);

    ResultadoPagoResponse estado(String token);
}
