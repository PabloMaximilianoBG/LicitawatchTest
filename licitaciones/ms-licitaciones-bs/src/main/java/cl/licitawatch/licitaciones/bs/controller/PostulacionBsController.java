package cl.licitawatch.licitaciones.bs.controller;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.licitaciones.bs.dto.request.PostularRequest;
import cl.licitawatch.licitaciones.bs.dto.response.ContextoPostulacionResponse;
import cl.licitawatch.licitaciones.bs.dto.response.PostulacionResponse;
import cl.licitawatch.licitaciones.bs.dto.response.UsoPlanResponse;
import cl.licitawatch.licitaciones.bs.service.PostulacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bs")
@RequiredArgsConstructor
public class PostulacionBsController {
    private final PostulacionService service;

    @PostMapping("/licitaciones/{id}/postulaciones")
    public ResponseEntity<PostulacionResponse> postular(UsuarioActual u, @PathVariable Integer id,
                                                        @Valid @RequestBody PostularRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.postular(u, id, request));
    }

    @GetMapping("/licitaciones/{id}/postulaciones")
    public ResponseEntity<List<PostulacionResponse>> postulantes(UsuarioActual u, @PathVariable Integer id) {
        return ResponseEntity.ok(service.postulantes(u, id));
    }

    @GetMapping("/postulaciones/mias")
    public ResponseEntity<List<PostulacionResponse>> mias(UsuarioActual u) {
        return ResponseEntity.ok(service.misPostulaciones(u));
    }

    @GetMapping("/postulaciones/uso")
    public ResponseEntity<UsoPlanResponse> uso(UsuarioActual u) {
        return ResponseEntity.ok(service.usoPlan(u));
    }

    @PatchMapping("/postulaciones/{id}/aprobar")
    public ResponseEntity<PostulacionResponse> aprobar(UsuarioActual u, @PathVariable Integer id) {
        return ResponseEntity.ok(service.aprobar(u, id));
    }

    @PatchMapping("/postulaciones/{id}/rechazar")
    public ResponseEntity<PostulacionResponse> rechazar(UsuarioActual u, @PathVariable Integer id) {
        return ResponseEntity.ok(service.rechazar(u, id));
    }

    /** Uso interno de MS.chat.bs. */
    @GetMapping("/postulaciones/{id}/contexto")
    public ResponseEntity<ContextoPostulacionResponse> contexto(@PathVariable Integer id) {
        return ResponseEntity.ok(service.contexto(id));
    }

    @GetMapping("/admin/postulaciones")
    public ResponseEntity<PaginaResponse<PostulacionResponse>> adminListar(UsuarioActual u,
                                                                           @RequestParam(required = false) String estado,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.adminListar(u, estado, page, size));
    }
}
