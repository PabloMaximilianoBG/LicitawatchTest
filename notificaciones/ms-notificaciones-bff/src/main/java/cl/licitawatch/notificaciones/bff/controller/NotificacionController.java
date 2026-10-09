package cl.licitawatch.notificaciones.bff.controller;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.notificaciones.bff.dto.request.SoporteRequest;
import cl.licitawatch.notificaciones.bff.dto.response.NotificacionResponse;
import cl.licitawatch.notificaciones.bff.dto.response.SoporteResponse;
import cl.licitawatch.notificaciones.bff.service.NotificacionBffService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificacionController {
    private final NotificacionBffService service;

    @Operation(summary = "Avisos por correo que recibió el usuario")
    @GetMapping("/notificaciones/mias")
    public ResponseEntity<PaginaResponse<NotificacionResponse>> mias(@RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.mias(page, size));
    }

    @Operation(summary = "Solicitud de soporte (estándar o prioritario según el plan)")
    @PostMapping("/soporte")
    public ResponseEntity<SoporteResponse> soporte(@Valid @RequestBody SoporteRequest request) {
        return ResponseEntity.ok(service.soporte(request));
    }

    @GetMapping("/admin/notificaciones")
    public ResponseEntity<PaginaResponse<NotificacionResponse>> admin(@RequestParam(required = false) String tipo,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.admin(tipo, page, size));
    }
}
