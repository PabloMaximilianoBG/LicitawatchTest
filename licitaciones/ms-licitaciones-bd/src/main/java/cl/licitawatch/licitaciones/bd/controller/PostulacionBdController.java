package cl.licitawatch.licitaciones.bd.controller;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.licitaciones.bd.dto.request.AdjudicarBdRequest;
import cl.licitawatch.licitaciones.bd.dto.request.EstadoRequest;
import cl.licitawatch.licitaciones.bd.dto.request.PostulacionBdRequest;
import cl.licitawatch.licitaciones.bd.dto.response.AdjudicacionBdResponse;
import cl.licitawatch.licitaciones.bd.dto.response.ConteoResponse;
import cl.licitawatch.licitaciones.bd.dto.response.PostulacionBdResponse;
import cl.licitawatch.licitaciones.bd.service.PostulacionDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bd")
@RequiredArgsConstructor
public class PostulacionBdController {
    private final PostulacionDataService service;

    @GetMapping("/postulaciones")
    public ResponseEntity<PaginaResponse<PostulacionBdResponse>> listar(@RequestParam(required = false) Integer licitacionId,
                                                                        @RequestParam(required = false) Integer pymeId,
                                                                        @RequestParam(required = false) String estado,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(service.listar(licitacionId, pymeId, estado, page, size));
    }

    @GetMapping("/licitaciones/{id}/postulaciones")
    public ResponseEntity<List<PostulacionBdResponse>> deLicitacion(@PathVariable Integer id) {
        return ResponseEntity.ok(service.deLicitacion(id));
    }

    @GetMapping("/postulaciones/{id}")
    public ResponseEntity<PostulacionBdResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping("/postulaciones")
    public ResponseEntity<PostulacionBdResponse> crear(@Valid @RequestBody PostulacionBdRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PatchMapping("/postulaciones/{id}/estado")
    public ResponseEntity<PostulacionBdResponse> estado(@PathVariable Integer id, @Valid @RequestBody EstadoRequest request) {
        return ResponseEntity.ok(service.cambiarEstado(id, request.estado()));
    }

    @GetMapping("/postulaciones/contar")
    public ResponseEntity<ConteoResponse> contar(@RequestParam Integer pymeId,
                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde) {
        return ResponseEntity.ok(new ConteoResponse(service.contarDesde(pymeId, desde)));
    }

    @GetMapping("/postulaciones/existe")
    public ResponseEntity<Boolean> existe(@RequestParam Integer licitacionId, @RequestParam Integer pymeId) {
        return ResponseEntity.ok(service.existe(licitacionId, pymeId));
    }

    @PostMapping("/licitaciones/{id}/adjudicar")
    public ResponseEntity<AdjudicacionBdResponse> adjudicar(@PathVariable Integer id, @Valid @RequestBody AdjudicarBdRequest request) {
        return ResponseEntity.ok(service.adjudicar(id, request.postulacionId()));
    }
}
