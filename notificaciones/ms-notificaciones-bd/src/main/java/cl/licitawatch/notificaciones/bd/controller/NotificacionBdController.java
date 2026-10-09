package cl.licitawatch.notificaciones.bd.controller;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.notificaciones.bd.dto.request.NotificacionBdRequest;
import cl.licitawatch.notificaciones.bd.dto.response.CatalogoResponse;
import cl.licitawatch.notificaciones.bd.dto.response.NotificacionBdResponse;
import cl.licitawatch.notificaciones.bd.service.NotificacionDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bd")
@RequiredArgsConstructor
public class NotificacionBdController {
    private final NotificacionDataService service;

    @PostMapping("/notificaciones")
    public ResponseEntity<NotificacionBdResponse> registrar(@Valid @RequestBody NotificacionBdRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(request));
    }

    @GetMapping("/notificaciones")
    public ResponseEntity<PaginaResponse<NotificacionBdResponse>> listar(@RequestParam(required = false) Integer usuarioId,
                                                                         @RequestParam(required = false) String tipo,
                                                                         @RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.listar(usuarioId, tipo, page, size));
    }

    @GetMapping("/tipos-notificacion")
    public ResponseEntity<List<CatalogoResponse>> tipos() {
        return ResponseEntity.ok(service.tipos());
    }
}
