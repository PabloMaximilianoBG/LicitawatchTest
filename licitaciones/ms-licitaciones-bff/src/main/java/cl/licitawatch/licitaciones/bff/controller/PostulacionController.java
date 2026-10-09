package cl.licitawatch.licitaciones.bff.controller;

import cl.licitawatch.licitaciones.bff.dto.response.PostulacionResponse;
import cl.licitawatch.licitaciones.bff.dto.response.UsoPlanResponse;
import cl.licitawatch.licitaciones.bff.service.LicitacionBffService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/postulaciones")
@RequiredArgsConstructor
public class PostulacionController {
    private final LicitacionBffService service;

    @Operation(summary = "Mis postulaciones y su estado (Pyme)")
    @GetMapping("/mias")
    public ResponseEntity<List<PostulacionResponse>> mias() {
        return ResponseEntity.ok(service.misPostulaciones());
    }

    @Operation(summary = "Uso del límite mensual de postulaciones del plan")
    @GetMapping("/uso")
    public ResponseEntity<UsoPlanResponse> uso() {
        return ResponseEntity.ok(service.uso());
    }

    @Operation(summary = "Aprobar (adjudica la licitación y rechaza las demás)")
    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<PostulacionResponse> aprobar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.aprobar(id));
    }

    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<PostulacionResponse> rechazar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.rechazar(id));
    }
}
