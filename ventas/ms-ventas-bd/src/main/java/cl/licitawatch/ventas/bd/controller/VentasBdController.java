package cl.licitawatch.ventas.bd.controller;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.ventas.bd.dto.request.EstadoRequest;
import cl.licitawatch.ventas.bd.dto.request.PagoBdRequest;
import cl.licitawatch.ventas.bd.dto.request.SuscripcionBdRequest;
import cl.licitawatch.ventas.bd.dto.request.VentaBdRequest;
import cl.licitawatch.ventas.bd.dto.response.*;
import cl.licitawatch.ventas.bd.service.VentasDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bd")
@RequiredArgsConstructor
public class VentasBdController {
    private final VentasDataService service;

    @GetMapping("/planes")
    public ResponseEntity<List<CatalogoResponse>> planes() {
        return ResponseEntity.ok(service.planes());
    }

    @GetMapping("/suscripciones")
    public ResponseEntity<PaginaResponse<SuscripcionBdResponse>> suscripciones(@RequestParam(required = false) Integer usuarioId,
                                                                               @RequestParam(required = false) String estado,
                                                                               @RequestParam(required = false) String plan,
                                                                               @RequestParam(defaultValue = "0") int page,
                                                                               @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.suscripciones(usuarioId, estado, plan, page, size));
    }

    @GetMapping("/suscripciones/usuario/{usuarioId}")
    public ResponseEntity<List<SuscripcionBdResponse>> deUsuario(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(service.suscripcionesDeUsuario(usuarioId));
    }

    @GetMapping("/suscripciones/{id}")
    public ResponseEntity<SuscripcionBdResponse> suscripcion(@PathVariable Integer id) {
        return ResponseEntity.ok(service.suscripcion(id));
    }

    @PostMapping("/suscripciones")
    public ResponseEntity<SuscripcionBdResponse> crearSuscripcion(@Valid @RequestBody SuscripcionBdRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearSuscripcion(request));
    }

    @PatchMapping("/suscripciones/{id}/estado")
    public ResponseEntity<SuscripcionBdResponse> estado(@PathVariable Integer id, @Valid @RequestBody EstadoRequest request) {
        return ResponseEntity.ok(service.cambiarEstadoSuscripcion(id, request.estado()));
    }

    @GetMapping("/suscripciones/usuarios-premium")
    public ResponseEntity<List<Integer>> usuariosPremium(@RequestParam List<Integer> usuarioIds) {
        return ResponseEntity.ok(service.usuariosPremium(usuarioIds));
    }

    @GetMapping("/suscripciones/premium-activas")
    public ResponseEntity<List<SuscripcionBdResponse>> premiumActivas() {
        return ResponseEntity.ok(service.premiumActivas());
    }

    @PostMapping("/ventas")
    public ResponseEntity<VentaBdResponse> crearVenta(@Valid @RequestBody VentaBdRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearVenta(request));
    }

    @GetMapping("/ventas/{id}")
    public ResponseEntity<VentaBdResponse> venta(@PathVariable Integer id) {
        return ResponseEntity.ok(service.venta(id));
    }

    @GetMapping("/ventas")
    public ResponseEntity<PaginaResponse<VentaBdResponse>> ventas(@RequestParam(required = false) Integer usuarioId,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.ventas(usuarioId, page, size));
    }

    @GetMapping("/ventas/pendientes-premium")
    public ResponseEntity<List<VentaBdResponse>> pendientes(@RequestParam Integer usuarioId) {
        return ResponseEntity.ok(service.ventasPendientesPremium(usuarioId));
    }

    @PostMapping("/ventas/{id}/pago")
    public ResponseEntity<VentaBdResponse> registrarPago(@PathVariable Integer id, @Valid @RequestBody PagoBdRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarPago(id, request));
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenBdResponse> resumen() {
        return ResponseEntity.ok(service.resumen());
    }
}
