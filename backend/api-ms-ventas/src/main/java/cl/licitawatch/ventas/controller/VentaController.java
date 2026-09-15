package cl.licitawatch.ventas.controller;

import cl.licitawatch.ventas.dto.ConfirmarPagoRequest;
import cl.licitawatch.ventas.dto.IniciarVentaRequest;
import cl.licitawatch.ventas.dto.IniciarVentaResponse;
import cl.licitawatch.ventas.dto.PagoResponse;
import cl.licitawatch.ventas.dto.VentaAdminResponse;
import cl.licitawatch.ventas.security.SecurityUtils;
import cl.licitawatch.ventas.service.VentaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping("/iniciar")
    public ResponseEntity<IniciarVentaResponse> iniciar(@Valid @RequestBody IniciarVentaRequest req) {
        IniciarVentaResponse respuesta = ventaService.iniciar(SecurityUtils.usuarioActual(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PostMapping("/confirmar")
    public PagoResponse confirmar(@Valid @RequestBody ConfirmarPagoRequest req) {
        return ventaService.confirmar(SecurityUtils.usuarioActual(), req);
    }

    @GetMapping("/todas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<VentaAdminResponse> listarTodas() {
        return ventaService.listarTodas();
    }
}
