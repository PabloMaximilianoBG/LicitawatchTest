package cl.licitawatch.ventas.bff.controller;

import cl.licitawatch.ventas.bff.dto.request.RetornoWebpayRequest;
import cl.licitawatch.ventas.bff.dto.response.*;
import cl.licitawatch.ventas.bff.service.VentasBffService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VentasController {
    private final VentasBffService service;

    @Operation(summary = "Planes de la Pyme: Estándar (gratis) y Premium (de pago)")
    @GetMapping("/planes")
    public ResponseEntity<List<PlanResponse>> planes() {
        return ResponseEntity.ok(service.planes());
    }

    @Operation(summary = "Plan vigente de la Pyme, vencimiento y compra pendiente")
    @GetMapping("/suscripciones/mi")
    public ResponseEntity<MiSuscripcionResponse> miSuscripcion() {
        return ResponseEntity.ok(service.miSuscripcion());
    }

    @Operation(summary = "Inicia el pago del plan Premium en Webpay Plus (sandbox)")
    @PostMapping("/ventas/premium")
    public ResponseEntity<IniciarPagoResponse> iniciarPremium() {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.iniciarPremium());
    }

    @GetMapping("/ventas/mias")
    public ResponseEntity<List<VentaResponse>> misVentas() {
        return ResponseEntity.ok(service.misVentas());
    }

    @GetMapping("/ventas/{id}")
    public ResponseEntity<VentaResponse> venta(@PathVariable Integer id) {
        return ResponseEntity.ok(service.venta(id));
    }

    @Operation(summary = "Retorno de Webpay (público): confirma el pago y redirige al frontend")
    @RequestMapping(value = "/pagos/webpay/retorno", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Void> retorno(@RequestParam(name = "token_ws", required = false) String tokenWs,
                                        @RequestParam(name = "TBK_TOKEN", required = false) String tbkToken,
                                        @RequestParam(name = "TBK_ORDEN_COMPRA", required = false) String tbkOrdenCompra,
                                        @RequestParam(name = "TBK_ID_SESION", required = false) String tbkIdSesion) {
        String destino = service.retornoWebpay(new RetornoWebpayRequest(tokenWs, tbkToken, tbkOrdenCompra, tbkIdSesion));
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, URI.create(destino).toString()).build();
    }
}
