package cl.licitawatch.notificaciones.bs.controller;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.notificaciones.bs.dto.request.CorreoEnlaceRequest;
import cl.licitawatch.notificaciones.bs.dto.request.NotificacionRequest;
import cl.licitawatch.notificaciones.bs.dto.request.SoporteRequest;
import cl.licitawatch.notificaciones.bs.dto.response.NotificacionResponse;
import cl.licitawatch.notificaciones.bs.dto.response.SoporteResponse;
import cl.licitawatch.notificaciones.bs.service.NotificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bs")
@RequiredArgsConstructor
public class NotificacionBsController {
    private final NotificacionService service;

    /** Uso interno: Licitaciones, Ventas y Chat informan un evento. */
    @PostMapping("/notificaciones")
    public ResponseEntity<NotificacionResponse> notificar(@Valid @RequestBody NotificacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.notificar(request));
    }

    @PostMapping("/correos/confirmacion-cuenta")
    public ResponseEntity<Void> confirmacion(@Valid @RequestBody CorreoEnlaceRequest request) {
        service.confirmacionCuenta(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/correos/restablecer-password")
    public ResponseEntity<Void> restablecer(@Valid @RequestBody CorreoEnlaceRequest request) {
        service.restablecerPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/soporte")
    public ResponseEntity<SoporteResponse> soporte(UsuarioActual u, @Valid @RequestBody SoporteRequest request) {
        return ResponseEntity.ok(service.soporte(u, request));
    }

    @GetMapping("/notificaciones/mias")
    public ResponseEntity<PaginaResponse<NotificacionResponse>> mias(UsuarioActual u, @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.mias(u, page, size));
    }

    @GetMapping("/admin/notificaciones")
    public ResponseEntity<PaginaResponse<NotificacionResponse>> admin(UsuarioActual u, @RequestParam(required = false) String tipo,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.adminListar(u, tipo, page, size));
    }
}
