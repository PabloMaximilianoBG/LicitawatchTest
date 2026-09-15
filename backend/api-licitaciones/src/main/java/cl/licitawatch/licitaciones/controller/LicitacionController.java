package cl.licitawatch.licitaciones.controller;

import cl.licitawatch.licitaciones.dto.ActualizarLicitacionRequest;
import cl.licitawatch.licitaciones.dto.CrearLicitacionRequest;
import cl.licitawatch.licitaciones.dto.LicitacionResponse;
import cl.licitawatch.licitaciones.security.SecurityUtils;
import cl.licitawatch.licitaciones.service.LicitacionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/licitaciones")
public class LicitacionController {

    private final LicitacionService licitacionService;

    public LicitacionController(LicitacionService licitacionService) {
        this.licitacionService = licitacionService;
    }

    @GetMapping
    public List<LicitacionResponse> buscar(
            @RequestParam(required = false) String rubro,
            @RequestParam(required = false) String region) {
        return licitacionService.buscarPublicadas(rubro, region);
    }

    @GetMapping("/{id}")
    public LicitacionResponse obtener(@PathVariable Long id) {
        return licitacionService.obtener(id);
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('EMPRESA')")
    public List<LicitacionResponse> misLicitaciones() {
        return licitacionService.misLicitaciones(SecurityUtils.usuarioActual().id());
    }

    @GetMapping("/todas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<LicitacionResponse> listarTodas() {
        return licitacionService.listarTodas();
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPRESA')")
    public ResponseEntity<LicitacionResponse> crear(@Valid @RequestBody CrearLicitacionRequest req) {
        LicitacionResponse creada = licitacionService.crear(SecurityUtils.usuarioActual(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPRESA')")
    public LicitacionResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarLicitacionRequest req) {
        return licitacionService.actualizar(id, SecurityUtils.usuarioActual(), req);
    }

    @PutMapping("/{id}/cerrar")
    @PreAuthorize("hasRole('EMPRESA')")
    public LicitacionResponse cerrar(@PathVariable Long id) {
        return licitacionService.cerrar(id, SecurityUtils.usuarioActual());
    }
}
