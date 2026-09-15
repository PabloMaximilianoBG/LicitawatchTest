package cl.licitawatch.licitaciones.controller;

import cl.licitawatch.licitaciones.dto.ActualizarEstadoPostulacionRequest;
import cl.licitawatch.licitaciones.dto.CrearPostulacionRequest;
import cl.licitawatch.licitaciones.dto.PostulacionResponse;
import cl.licitawatch.licitaciones.security.SecurityUtils;
import cl.licitawatch.licitaciones.service.PostulacionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PostulacionController {

    private final PostulacionService postulacionService;

    public PostulacionController(PostulacionService postulacionService) {
        this.postulacionService = postulacionService;
    }

    @PostMapping("/licitaciones/{licitacionId}/postulaciones")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PostulacionResponse> postular(
            @PathVariable Long licitacionId, @Valid @RequestBody CrearPostulacionRequest req) {
        PostulacionResponse creada = postulacionService.postular(licitacionId, SecurityUtils.usuarioActual(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping("/licitaciones/{licitacionId}/postulaciones")
    @PreAuthorize("hasRole('EMPRESA')")
    public List<PostulacionResponse> postulacionesDeLicitacion(@PathVariable Long licitacionId) {
        return postulacionService.postulacionesDeLicitacion(licitacionId, SecurityUtils.usuarioActual());
    }

    @PutMapping("/licitaciones/{licitacionId}/postulaciones/{postulacionId}/estado")
    @PreAuthorize("hasRole('EMPRESA')")
    public PostulacionResponse actualizarEstado(
            @PathVariable Long licitacionId, @PathVariable Long postulacionId,
            @Valid @RequestBody ActualizarEstadoPostulacionRequest req) {
        return postulacionService.actualizarEstado(licitacionId, postulacionId, SecurityUtils.usuarioActual(), req.estado());
    }

    @GetMapping("/postulaciones/mias")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<PostulacionResponse> misPostulaciones() {
        return postulacionService.misPostulaciones(SecurityUtils.usuarioActual().id());
    }
}
