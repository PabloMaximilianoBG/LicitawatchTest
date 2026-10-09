package cl.licitawatch.licitaciones.bs.controller;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.licitaciones.bs.dto.request.BusquedaRequest;
import cl.licitawatch.licitaciones.bs.dto.request.CambiarEstadoLicitacionRequest;
import cl.licitawatch.licitaciones.bs.dto.request.LicitacionRequest;
import cl.licitawatch.licitaciones.bs.dto.response.ArchivoDescarga;
import cl.licitawatch.licitaciones.bs.dto.response.CatalogosResponse;
import cl.licitawatch.licitaciones.bs.dto.response.LicitacionResponse;
import cl.licitawatch.licitaciones.bs.service.LicitacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/bs")
@RequiredArgsConstructor
public class LicitacionBsController {
    private final LicitacionService service;

    @GetMapping("/licitaciones")
    public ResponseEntity<PaginaResponse<LicitacionResponse>> buscar(UsuarioActual u,
                                                                     @RequestParam(required = false) String q,
                                                                     @RequestParam(required = false) Integer rubroId,
                                                                     @RequestParam(required = false) Integer regionId,
                                                                     @RequestParam(required = false) BigDecimal presupuestoMin,
                                                                     @RequestParam(required = false) BigDecimal presupuestoMax,
                                                                     @RequestParam(required = false) String orden,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(service.buscar(u, BusquedaRequest.builder().q(q).rubroId(rubroId).regionId(regionId)
                .presupuestoMin(presupuestoMin).presupuestoMax(presupuestoMax).orden(orden).page(page).size(size).build()));
    }

    @GetMapping("/licitaciones/mias")
    public ResponseEntity<List<LicitacionResponse>> mias(UsuarioActual u) {
        return ResponseEntity.ok(service.misLicitaciones(u));
    }

    @GetMapping("/licitaciones/catalogos")
    public ResponseEntity<CatalogosResponse> catalogos() {
        return ResponseEntity.ok(service.catalogos());
    }

    @GetMapping("/licitaciones/{id}")
    public ResponseEntity<LicitacionResponse> obtener(UsuarioActual u, @PathVariable Integer id) {
        return ResponseEntity.ok(service.obtener(u, id));
    }

    @PostMapping("/licitaciones")
    public ResponseEntity<LicitacionResponse> crear(UsuarioActual u, @Valid @RequestBody LicitacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(u, request));
    }

    @PutMapping("/licitaciones/{id}")
    public ResponseEntity<LicitacionResponse> actualizar(UsuarioActual u, @PathVariable Integer id,
                                                         @Valid @RequestBody LicitacionRequest request) {
        return ResponseEntity.ok(service.actualizar(u, id, request));
    }

    @PatchMapping("/licitaciones/{id}/cerrar")
    public ResponseEntity<LicitacionResponse> cerrar(UsuarioActual u, @PathVariable Integer id) {
        return ResponseEntity.ok(service.cerrar(u, id));
    }

    @DeleteMapping("/licitaciones/{id}")
    public ResponseEntity<Void> eliminar(UsuarioActual u, @PathVariable Integer id) {
        service.eliminar(u, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/licitaciones/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LicitacionResponse> subirImagen(UsuarioActual u, @PathVariable Integer id,
                                                          @RequestPart("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(service.subirImagen(u, id, archivo));
    }

    @DeleteMapping("/licitaciones/{id}/imagen")
    public ResponseEntity<LicitacionResponse> quitarImagen(UsuarioActual u, @PathVariable Integer id) {
        return ResponseEntity.ok(service.quitarImagen(u, id));
    }

    @PostMapping(value = "/licitaciones/{id}/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LicitacionResponse> subirDocumento(UsuarioActual u, @PathVariable Integer id,
                                                             @RequestPart("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(service.subirDocumento(u, id, archivo));
    }

    @DeleteMapping("/licitaciones/{id}/archivo")
    public ResponseEntity<LicitacionResponse> quitarDocumento(UsuarioActual u, @PathVariable Integer id) {
        return ResponseEntity.ok(service.quitarDocumento(u, id));
    }

    @GetMapping("/archivos/{licitacionId}/{nombre}")
    public ResponseEntity<byte[]> archivo(@PathVariable Integer licitacionId, @PathVariable String nombre) {
        ArchivoDescarga a = service.archivo(licitacionId, nombre);
        ContentDisposition cd = (a.inline() ? ContentDisposition.inline() : ContentDisposition.attachment()).filename(a.nombre()).build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(a.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString()).header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600").body(a.contenido());
    }

    @GetMapping("/admin/licitaciones")
    public ResponseEntity<PaginaResponse<LicitacionResponse>> adminListar(UsuarioActual u,
                                                                          @RequestParam(required = false) String q,
                                                                          @RequestParam(required = false) String estado,
                                                                          @RequestParam(required = false) Integer rubroId,
                                                                          @RequestParam(required = false) Integer regionId,
                                                                          @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.adminListar(u, BusquedaRequest.builder().q(q).estado(estado).rubroId(rubroId)
                .regionId(regionId).page(page).size(size).build()));
    }

    @PatchMapping("/admin/licitaciones/{id}/estado")
    public ResponseEntity<LicitacionResponse> adminEstado(UsuarioActual u, @PathVariable Integer id,
                                                          @Valid @RequestBody CambiarEstadoLicitacionRequest request) {
        return ResponseEntity.ok(service.adminCambiarEstado(u, id, request.estado()));
    }
}
