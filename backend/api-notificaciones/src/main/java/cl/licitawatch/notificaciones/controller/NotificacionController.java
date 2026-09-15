package cl.licitawatch.notificaciones.controller;

import cl.licitawatch.notificaciones.dto.NotificacionIngestaRequest;
import cl.licitawatch.notificaciones.dto.NotificacionResponse;
import cl.licitawatch.notificaciones.service.NotificacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Unico endpoint de este microservicio (seccion 3.4 del diseno): ingesta de
 * notificaciones desde API Licitaciones y API MS-Ventas. No expone nada mas
 * a proposito. Protegido solo por el binding a 127.0.0.1 y por no tener ruta
 * en el Gateway - igual que /internal/** en los demas servicios.
 */
@RestController
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @PostMapping("/notificaciones")
    public ResponseEntity<NotificacionResponse> recibir(@Valid @RequestBody NotificacionIngestaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificacionService.recibir(req));
    }
}
