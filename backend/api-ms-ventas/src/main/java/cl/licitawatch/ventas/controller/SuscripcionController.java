package cl.licitawatch.ventas.controller;

import cl.licitawatch.ventas.dto.SuscripcionResponse;
import cl.licitawatch.ventas.security.SecurityUtils;
import cl.licitawatch.ventas.service.SuscripcionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    public SuscripcionController(SuscripcionService suscripcionService) {
        this.suscripcionService = suscripcionService;
    }

    @GetMapping("/api/suscripciones/mia")
    public SuscripcionResponse miSuscripcion() {
        return suscripcionService.obtenerVigente(SecurityUtils.usuarioActual().id());
    }

    /**
     * Endpoint interno para que API Licitaciones / API Asistente consulten el
     * plan vigente de un usuario sin tener acceso a esta base de datos.
     */
    @GetMapping("/internal/suscripciones/{usuarioId}")
    public SuscripcionResponse suscripcionDe(@PathVariable Long usuarioId) {
        return suscripcionService.obtenerVigente(usuarioId);
    }
}
