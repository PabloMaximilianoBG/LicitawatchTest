package cl.licitawatch.ventas;

import cl.licitawatch.ventas.pago.ConfirmacionPagoResultado;
import cl.licitawatch.ventas.pago.InicioPagoResultado;
import cl.licitawatch.ventas.pago.PasarelaPagoAdapter;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Primary;

/**
 * Reemplaza a TransbankWebpayAdapter en los tests para no depender de red ni
 * del sandbox real de Transbank. El resultado de confirmar() se controla con
 * setAprobarSiguiente(...) desde el propio test, justo antes de llamarlo.
 */
@TestComponent
@Primary
public class FakePasarelaPagoAdapter implements PasarelaPagoAdapter {

    private final AtomicBoolean aprobarSiguiente = new AtomicBoolean(true);

    public void setAprobarSiguiente(boolean valor) {
        aprobarSiguiente.set(valor);
    }

    @Override
    public InicioPagoResultado iniciar(String buyOrder, String sessionId, BigDecimal monto, String returnUrl) {
        return new InicioPagoResultado("tok-" + buyOrder, "http://fake-webpay.test/redirect");
    }

    @Override
    public ConfirmacionPagoResultado confirmar(String token) {
        boolean aprobado = aprobarSiguiente.get();
        return new ConfirmacionPagoResultado(aprobado, aprobado ? "AUT123" : null, BigDecimal.TEN, aprobado ? "6623" : null);
    }
}
