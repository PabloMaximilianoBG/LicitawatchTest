package cl.licitawatch.ventas.ambassador.service.impl;

import cl.licitawatch.common.exception.LicitaWatchException;
import cl.licitawatch.ventas.ambassador.dto.request.CrearTransaccionRequest;
import cl.licitawatch.ventas.ambassador.dto.response.ResultadoPagoResponse;
import cl.licitawatch.ventas.ambassador.dto.response.TransaccionResponse;
import cl.licitawatch.ventas.ambassador.service.PasarelaPagoService;
import cl.transbank.webpay.webpayplus.WebpayPlus;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCreateResponse;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Adaptador Webpay Plus (SDK oficial transbank-sdk-java). Ambiente SANDBOX/INTEGRATION = webpay3gint.transbank.cl
 * con el comercio de pruebas 597055555532: tarjetas de prueba, sin cobros reales.
 */
@Slf4j
@Service
public class WebpayPasarelaPagoAdapter implements PasarelaPagoService {
    private final WebpayPlus.Transaction tx;

    public WebpayPasarelaPagoAdapter(@Value("${licitawatch.webpay.commerce-code}") String commerceCode,
                                     @Value("${licitawatch.webpay.api-key}") String apiKey,
                                     @Value("${licitawatch.webpay.environment:SANDBOX}") String environment) {
        boolean produccion = "PRODUCTION".equalsIgnoreCase(environment);
        this.tx = produccion ? WebpayPlus.Transaction.buildForProduction(commerceCode, apiKey)
                : WebpayPlus.Transaction.buildForIntegration(commerceCode, apiKey);
        log.info("Webpay Plus en ambiente {} (comercio {})", produccion ? "PRODUCTION" : "SANDBOX (sin cobros reales)", commerceCode);
    }

    @Override
    public TransaccionResponse crearTransaccion(CrearTransaccionRequest r) {
        try {
            WebpayPlusTransactionCreateResponse resp = tx.create(r.ordenCompra(), r.sesionId(), r.monto().doubleValue(), r.urlRetorno());
            return new TransaccionResponse(resp.getToken(), resp.getUrl());
        } catch (Exception e) {
            log.error("Webpay create falló: {}", e.getMessage());
            throw new PasarelaException("No se pudo iniciar el pago en Webpay");
        }
    }

    @Override
    public ResultadoPagoResponse confirmar(String token) {
        try {
            return map(tx.commit(token));
        } catch (Exception e) {
            log.error("Webpay commit falló: {}", e.getMessage());
            throw new PasarelaException("No se pudo confirmar el pago en Webpay");
        }
    }

    @Override
    public ResultadoPagoResponse estado(String token) {
        try {
            return map(tx.status(token));
        } catch (Exception e) {
            log.error("Webpay status falló: {}", e.getMessage());
            throw new PasarelaException("No se pudo consultar el pago en Webpay");
        }
    }

    static ResultadoPagoResponse map(WebpayPlusTransactionStatusResponse r) {
        String tarjeta = r.getCardDetail() != null ? r.getCardDetail().getCardNumber() : null;
        String ultimos = tarjeta != null && tarjeta.length() >= 4 ? tarjeta.substring(tarjeta.length() - 4) : tarjeta;
        boolean aprobado = "AUTHORIZED".equals(r.getStatus()) && r.getResponseCode() == 0;
        return ResultadoPagoResponse.builder().aprobado(aprobado).estado(r.getStatus()).codigoRespuesta((int) r.getResponseCode())
                .monto(BigDecimal.valueOf(r.getAmount())).ordenCompra(r.getBuyOrder()).codigoAutorizacion(r.getAuthorizationCode())
                .tipoPago(r.getPaymentTypeCode()).metodoPago(metodo(r.getPaymentTypeCode())).ultimosDigitos(ultimos)
                .fechaTransaccion(r.getTransactionDate()).build();
    }

    /** Traduce el tipo de pago de Transbank al catálogo metodo_pago: VD (débito) y VP (prepago) = Débito; el resto = Crédito. */
    static String metodo(String paymentTypeCode) {
        if (paymentTypeCode == null) {
            return "Crédito";
        }
        return switch (paymentTypeCode) {
            case "VD", "VP" -> "Débito";
            default -> "Crédito";
        };
    }

    static class PasarelaException extends LicitaWatchException {
        PasarelaException(String mensaje) {
            super(HttpStatus.BAD_GATEWAY, "PASARELA_ERROR", mensaje);
        }
    }
}
