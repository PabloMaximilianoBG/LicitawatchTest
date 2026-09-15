package cl.licitawatch.ventas.pago;

import java.math.BigDecimal;

/**
 * Abstrae el proveedor de pago concreto (seccion 1 del prompt: "implementa
 * el pago detras de una interfaz PasarelaPagoAdapter para que el proveedor
 * concreto sea intercambiable... sin tocar el resto de API MS-Ventas").
 * Implementacion actual: TransbankWebpayAdapter (Webpay Plus, sandbox).
 */
public interface PasarelaPagoAdapter {

    InicioPagoResultado iniciar(String buyOrder, String sessionId, BigDecimal monto, String returnUrl);

    ConfirmacionPagoResultado confirmar(String token);
}
