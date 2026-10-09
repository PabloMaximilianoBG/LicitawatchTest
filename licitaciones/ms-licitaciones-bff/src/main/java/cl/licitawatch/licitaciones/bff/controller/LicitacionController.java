package cl.licitawatch.licitaciones.bff.controller;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.licitaciones.bff.dto.request.LicitacionRequest;
import cl.licitawatch.licitaciones.bff.dto.request.PostularRequest;
import cl.licitawatch.licitaciones.bff.dto.response.CatalogosResponse;
import cl.licitawatch.licitaciones.bff.dto.response.LicitacionResponse;
import cl.licitawatch.licitaciones.bff.dto.response.PostulacionResponse;
import cl.licitawatch.licitaciones.bff.service.LicitacionBffService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/licitaciones")
@RequiredArgsConstructor
public class LicitacionController {
    private final LicitacionBffService service;

    @Operation(summary = "Buscar y filtrar licitaciones abiertas (filtro rubroId = mismo id del perfil)")
    @GetMapping
    public ResponseEntity<PaginaResponse<LicitacionResponse>> buscar(@RequestParam(required = false) String q,
                                                                     @RequestParam(required = false) Integer rubroId,
                                                                     @RequestParam(required = false) Integer regionId,
                                                                     @RequestParam(required = false) BigDecimal presupuestoMin,
                                                                     @RequestParam(required = false) BigDecimal presupuestoMax,
                                                                     @RequestParam(required = false) String orden,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(service.buscar(q, rubroId, regionId, presupuestoMin, presupuestoMax, orden, page, size));
    }

    @Operation(summary = "Licitaciones del Licitador logueado")
    @GetMapping("/mias")
    public ResponseEntity<List<LicitacionResponse>> mias() {
        return ResponseEntity.ok(service.mias());
    }

    @GetMapping("/catalogos")
    public ResponseEntity<CatalogosResponse> catalogos() {
        return ResponseEntity.ok(service.catalogos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LicitacionResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @Operation(summary = "Publicar licitación (Licitador, sin costo)")
    @PostMapping
    public ResponseEntity<LicitacionResponse> crear(@Valid @RequestBody LicitacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LicitacionResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody LicitacionRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<LicitacionResponse> cerrar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.cerrar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Subir imagen (la URL se genera sola)")
    @PostMapping(value = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LicitacionResponse> subirImagen(@PathVariable Integer id, @RequestPart("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(service.subirImagen(id, archivo));
    }

    @DeleteMapping("/{id}/imagen")
    public ResponseEntity<LicitacionResponse> quitarImagen(@PathVariable Integer id) {
        return ResponseEntity.ok(service.quitarImagen(id));
    }

    @Operation(summary = "Subir documento complementario (PDF, DOCX, XLSX u otro; la URL se genera sola)")
    @PostMapping(value = "/{id}/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LicitacionResponse> subirDocumento(@PathVariable Integer id, @RequestPart("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(service.subirDocumento(id, archivo));
    }

    @DeleteMapping("/{id}/archivo")
    public ResponseEntity<LicitacionResponse> quitarDocumento(@PathVariable Integer id) {
        return ResponseEntity.ok(service.quitarDocumento(id));
    }

    @GetMapping("/archivos/{licitacionId}/{nombre}")
    public ResponseEntity<byte[]> archivo(@PathVariable Integer licitacionId, @PathVariable String nombre) {
        return service.archivo(licitacionId, nombre);
    }

    @Operation(summary = "Postular (Pyme). Nace en estado Pendiente")
    @PostMapping("/{id}/postulaciones")
    public ResponseEntity<PostulacionResponse> postular(@PathVariable Integer id, @Valid @RequestBody PostularRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.postular(id, request));
    }

    @Operation(summary = "Ver postulantes (Licitador dueño o Administrador). Premium primero")
    @GetMapping("/{id}/postulaciones")
    public ResponseEntity<List<PostulacionResponse>> postulantes(@PathVariable Integer id) {
        return ResponseEntity.ok(service.postulantes(id));
    }
}
