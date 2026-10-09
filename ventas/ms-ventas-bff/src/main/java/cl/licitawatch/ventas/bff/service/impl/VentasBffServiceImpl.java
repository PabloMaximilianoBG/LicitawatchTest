package cl.licitawatch.ventas.bff.service.impl;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.ventas.bff.client.VentasBsClient;
import cl.licitawatch.ventas.bff.dto.request.RetornoWebpayRequest;
import cl.licitawatch.ventas.bff.dto.response.*;
import cl.licitawatch.ventas.bff.service.VentasBffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentasBffServiceImpl implements VentasBffService {
    private final VentasBsClient bs;

    @Value("${licitawatch.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public List<PlanResponse> planes() {
        return bs.planes();
    }

    @Override
    public MiSuscripcionResponse miSuscripcion() {
        return bs.miSuscripcion();
    }

    @Override
    public IniciarPagoResponse iniciarPremium() {
        return bs.iniciarPremium();
    }

    @Override
    public List<VentaResponse> misVentas() {
        return bs.misVentas();
    }

    @Override
    public VentaResponse venta(Integer id) {
        return bs.venta(id);
    }

    @Override
    public String retornoWebpay(RetornoWebpayRequest request) {
        try {
            return bs.retorno(request).redirectUrl();
        } catch (Exception e) {
            log.error("Error procesando el retorno de Webpay: {}", e.getMessage());
            return frontendUrl + "/pago/resultado?estado=error";
        }
    }

    @Override
    public PaginaResponse<VentaResponse> adminVentas(int page, int size) {
        return bs.adminVentas(page, size);
    }

    @Override
    public ResumenVentasResponse resumen() {
        return bs.resumen();
    }

    @Override
    public PaginaResponse<SuscripcionResponse> adminSuscripciones(String estado, String plan, int page, int size) {
        return bs.adminSuscripciones(estado, plan, page, size);
    }

    @Override
    public SuscripcionResponse cancelar(Integer id) {
        return bs.cancelar(id);
    }
}
