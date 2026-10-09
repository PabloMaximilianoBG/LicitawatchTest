package cl.licitawatch.licitaciones.bd.controller;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.licitaciones.bd.dto.request.ArchivosBdRequest;
import cl.licitawatch.licitaciones.bd.dto.request.EstadoRequest;
import cl.licitawatch.licitaciones.bd.dto.request.LicitacionBdRequest;
import cl.licitawatch.licitaciones.bd.dto.response.CatalogosBdResponse;
import cl.licitawatch.licitaciones.bd.dto.response.EliminacionBdResponse;
import cl.licitawatch.licitaciones.bd.dto.response.LicitacionBdResponse;
import cl.licitawatch.licitaciones.bd.service.LicitacionDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bd")
@RequiredArgsConstructor
public class LicitacionBdController {
    private final LicitacionDataService service;

    @GetMapping("/catalogos")
    public ResponseEntity<CatalogosBdResponse> catalogos() {
        return ResponseEntity.ok(service.catalogos());
    }

    @GetMapping("/licitaciones")
    public ResponseEntity<PaginaResponse<LicitacionBdResponse>> buscar(
            @RequestParam(required = false) String q, @RequestParam(required = false) Integer rubroId,
            @RequestParam(required = false) Integer regionId, @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer licitadorId, @RequestParam(required = false) BigDecimal presupuestoMin,
            @RequestParam(required = false) BigDecimal presupuestoMax,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vigenteDesde,
            @RequestParam(required = false) Boolean conCupo, @RequestParam(required = false) String orden,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.buscar(new LicitacionDataService.Filtros(q, rubroId, regionId, estado, licitadorId,
                presupuestoMin, presupuestoMax, vigenteDesde, conCupo, orden, page, size)));
    }

    @GetMapping("/licitaciones/{id}")
    public ResponseEntity<LicitacionBdResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping("/licitaciones/por-ids")
    public ResponseEntity<List<LicitacionBdResponse>> porIds(@RequestParam List<Integer> ids) {
        return ResponseEntity.ok(service.porIds(ids));
    }

    @GetMapping("/licitaciones/vencidas")
    public ResponseEntity<List<LicitacionBdResponse>> vencidas(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(service.vencidas(fecha));
    }

    @PostMapping("/licitaciones")
    public ResponseEntity<LicitacionBdResponse> crear(@Valid @RequestBody LicitacionBdRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/licitaciones/{id}")
    public ResponseEntity<LicitacionBdResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody LicitacionBdRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/licitaciones/{id}/estado")
    public ResponseEntity<LicitacionBdResponse> estado(@PathVariable Integer id, @Valid @RequestBody EstadoRequest request) {
        return ResponseEntity.ok(service.cambiarEstado(id, request.estado()));
    }

    @PatchMapping("/licitaciones/{id}/archivos")
    public ResponseEntity<LicitacionBdResponse> archivos(@PathVariable Integer id, @RequestBody ArchivosBdRequest request) {
        return ResponseEntity.ok(service.actualizarArchivos(id, request));
    }

    @DeleteMapping("/licitaciones/{id}")
    public ResponseEntity<EliminacionBdResponse> eliminar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.eliminar(id));
    }
}
