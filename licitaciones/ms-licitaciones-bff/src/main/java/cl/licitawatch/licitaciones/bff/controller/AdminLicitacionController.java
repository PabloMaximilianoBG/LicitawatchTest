package cl.licitawatch.licitaciones.bff.controller;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.licitaciones.bff.dto.request.CambiarEstadoLicitacionRequest;
import cl.licitawatch.licitaciones.bff.dto.request.LicitacionRequest;
import cl.licitawatch.licitaciones.bff.dto.response.LicitacionResponse;
import cl.licitawatch.licitaciones.bff.dto.response.PostulacionResponse;
import cl.licitawatch.licitaciones.bff.service.LicitacionBffService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Moderación de licitaciones (Administrador). */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminLicitacionController {
    private final LicitacionBffService service;

    @GetMapping("/licitaciones")
    public ResponseEntity<PaginaResponse<LicitacionResponse>> listar(@RequestParam(required = false) String q,
                                                                     @RequestParam(required = false) String estado,
                                                                     @RequestParam(required = false) Integer rubroId,
                                                                     @RequestParam(required = false) Integer regionId,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.adminListar(q, estado, rubroId, regionId, page, size));
    }

    @GetMapping("/licitaciones/{id}")
    public ResponseEntity<LicitacionResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PutMapping("/licitaciones/{id}")
    public ResponseEntity<LicitacionResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody LicitacionRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @Operation(summary = "Cerrar o reabrir una licitación")
    @PatchMapping("/licitaciones/{id}/estado")
    public ResponseEntity<LicitacionResponse> estado(@PathVariable Integer id, @Valid @RequestBody CambiarEstadoLicitacionRequest request) {
        return ResponseEntity.ok(service.adminEstado(id, request.estado()));
    }

    @DeleteMapping("/licitaciones/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/licitaciones/{id}/postulaciones")
    public ResponseEntity<List<PostulacionResponse>> postulaciones(@PathVariable Integer id) {
        return ResponseEntity.ok(service.postulantes(id));
    }

    @GetMapping("/postulaciones")
    public ResponseEntity<PaginaResponse<PostulacionResponse>> todasLasPostulaciones(@RequestParam(required = false) String estado,
                                                                                    @RequestParam(defaultValue = "0") int page,
                                                                                    @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.adminPostulaciones(estado, page, size));
    }
}
