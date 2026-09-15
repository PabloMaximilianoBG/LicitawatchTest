package cl.licitawatch.ventas.pago;

import cl.licitawatch.ventas.exception.PagoException;
import cl.transbank.webpay.webpayplus.WebpayPlus;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCommitResponse;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCreateResponse;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Implementacion concreta de PasarelaPagoAdapter usando el SDK oficial de
 * Transbank (Webpay Plus, ambiente de integracion/sandbox por defecto segun
 * TRANSBANK_ENVIRONMENT). El resto de MS-Ventas solo conoce la interfaz
 * PasarelaPagoAdapter, nunca esta clase directamente.
 */
@Component
public class TransbankWebpayAdapter implements PasarelaPagoAdapter {

    private final WebpayPlus.Transaction transaction;

    public TransbankWebpayAdapter(
            @Value("${licitawatch.transbank.environment}") String environment,
            @Value("${licitawatch.transbank.api-key-id}") String apiKeyId,
            @Value("${licitawatch.transbank.api-key-secret}") String apiKeySecret) {
        this.transaction = "production".equalsIgnoreCase(environment)
                ? WebpayPlus.Transaction.buildForProduction(apiKeyId, apiKeySecret)
                : WebpayPlus.Transaction.buildForIntegration(apiKeyId, apiKeySecret);
    }

    @Override
    public InicioPagoResultado iniciar(String buyOrder, String sessionId, BigDecimal monto, String returnUrl) {
        try {
            WebpayPlusTransactionCreateResponse respuesta = transaction.create(buyOrder, sessionId, monto.doubleValue(), returnUrl);
            return new InicioPagoResultado(respuesta.getToken(), respuesta.getUrl());
        } catch (Exception ex) {
            throw new PagoException("No se pudo iniciar el pago con la pasarela", ex);
        }
    }

    @Override
    public ConfirmacionPagoResultado confirmar(String token) {
        try {
            WebpayPlusTransactionCommitResponse respuesta = transaction.commit(token);
            boolean aprobado = "AUTHORIZED".equals(respuesta.getStatus()) && respuesta.getResponseCode() == 0;
            String ultimosDigitos = respuesta.getCardDetail() != null ? respuesta.getCardDetail().getCardNumber() : null;
            return new ConfirmacionPagoResultado(aprobado, respuesta.getAuthorizationCode(), BigDecimal.valueOf(respuesta.getAmount()), ultimosDigitos);
        } catch (Exception ex) {
            throw new PagoException("No se pudo confirmar el pago con la pasarela", ex);
        }
    }
}
