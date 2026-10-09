package cl.licitawatch.ventas.bff.controller;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.ventas.bff.dto.response.ResumenVentasResponse;
import cl.licitawatch.ventas.bff.dto.response.SuscripcionResponse;
import cl.licitawatch.ventas.bff.dto.response.VentaResponse;
import cl.licitawatch.ventas.bff.service.VentasBffService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** PPT diap. 6: el Administrador ve ventas y suscripciones. */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminVentasController {
    private final VentasBffService service;

    @GetMapping("/ventas")
    public ResponseEntity<PaginaResponse<VentaResponse>> ventas(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.adminVentas(page, size));
    }

    @GetMapping("/ventas/resumen")
    public ResponseEntity<ResumenVentasResponse> resumen() {
        return ResponseEntity.ok(service.resumen());
    }

    @GetMapping("/suscripciones")
    public ResponseEntity<PaginaResponse<SuscripcionResponse>> suscripciones(@RequestParam(required = false) String estado,
                                                                             @RequestParam(required = false) String plan,
                                                                             @RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.adminSuscripciones(estado, plan, page, size));
    }

    @Operation(summary = "Cancelar un Premium activo (la Pyme vuelve a Estándar)")
    @PatchMapping("/suscripciones/{id}/cancelar")
    public ResponseEntity<SuscripcionResponse> cancelar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.cancelar(id));
    }
}
