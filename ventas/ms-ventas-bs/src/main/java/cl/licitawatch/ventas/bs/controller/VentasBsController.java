package cl.licitawatch.ventas.bs.controller;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.ventas.bs.dto.request.RetornoWebpayRequest;
import cl.licitawatch.ventas.bs.dto.request.UsuarioIdRequest;
import cl.licitawatch.ventas.bs.dto.response.*;
import cl.licitawatch.ventas.bs.service.PagoService;
import cl.licitawatch.ventas.bs.service.PlanService;
import cl.licitawatch.ventas.bs.service.SuscripcionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bs")
@RequiredArgsConstructor
public class VentasBsController {
    private final PlanService planService;
    private final SuscripcionService suscripcionService;
    private final PagoService pagoService;

    @GetMapping("/planes")
    public ResponseEntity<List<PlanResponse>> planes() {
        return ResponseEntity.ok(planService.listar());
    }

    @GetMapping("/suscripciones/mi")
    public ResponseEntity<MiSuscripcionResponse> mi(UsuarioActual u) {
        return ResponseEntity.ok(suscripcionService.miSuscripcion(u));
    }

    /** Uso interno (MS.usuarios.bs al registrar una Pyme): plan Estándar por defecto. */
    @PostMapping("/suscripciones/estandar")
    public ResponseEntity<Void> estandar(@Valid @RequestBody UsuarioIdRequest request) {
        suscripcionService.asignarEstandar(request.usuarioId());
        return ResponseEntity.noContent().build();
    }

    /** Uso interno (Licitaciones, Asistente): plan vigente y límite de postulaciones. */
    @GetMapping("/suscripciones/plan-vigente/{usuarioId}")
    public ResponseEntity<PlanVigenteResponse> planVigente(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(suscripcionService.planVigente(usuarioId));
    }

    /** Uso interno (Usuarios): qué usuarios tienen Premium vigente (insignia y prioridad). */
    @GetMapping("/suscripciones/premium")
    public ResponseEntity<List<Integer>> premium(@RequestParam List<Integer> usuarioIds) {
        return ResponseEntity.ok(suscripcionService.usuariosPremium(usuarioIds));
    }

    @PostMapping("/ventas/premium")
    public ResponseEntity<IniciarPagoResponse> iniciarPremium(UsuarioActual u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.iniciarPremium(u));
    }

    @GetMapping("/ventas/mias")
    public ResponseEntity<List<VentaResponse>> misVentas(UsuarioActual u) {
        return ResponseEntity.ok(pagoService.misVentas(u));
    }

    @GetMapping("/ventas/{id}")
    public ResponseEntity<VentaResponse> venta(UsuarioActual u, @PathVariable Integer id) {
        return ResponseEntity.ok(pagoService.venta(u, id));
    }

    @PostMapping("/pagos/webpay/retorno")
    public ResponseEntity<RetornoResponse> retorno(@RequestBody RetornoWebpayRequest request) {
        return ResponseEntity.ok(pagoService.procesarRetorno(request));
    }

    @GetMapping("/admin/ventas")
    public ResponseEntity<PaginaResponse<VentaResponse>> adminVentas(UsuarioActual u, @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(pagoService.adminVentas(u, page, size));
    }

    @GetMapping("/admin/ventas/resumen")
    public ResponseEntity<ResumenVentasResponse> resumen(UsuarioActual u) {
        return ResponseEntity.ok(pagoService.resumen(u));
    }

    @GetMapping("/admin/suscripciones")
    public ResponseEntity<PaginaResponse<SuscripcionResponse>> adminSuscripciones(UsuarioActual u,
                                                                                  @RequestParam(required = false) String estado,
                                                                                  @RequestParam(required = false) String plan,
                                                                                  @RequestParam(defaultValue = "0") int page,
                                                                                  @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(suscripcionService.adminListar(u, estado, plan, page, size));
    }

    @PatchMapping("/admin/suscripciones/{id}/cancelar")
    public ResponseEntity<SuscripcionResponse> cancelar(UsuarioActual u, @PathVariable Integer id) {
        return ResponseEntity.ok(suscripcionService.adminCancelar(u, id));
    }
}
