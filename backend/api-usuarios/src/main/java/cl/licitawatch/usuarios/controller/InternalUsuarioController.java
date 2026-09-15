package cl.licitawatch.usuarios.controller;

import cl.licitawatch.usuarios.dto.UsuarioInternoResponse;
import cl.licitawatch.usuarios.service.PerfilService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint interno para que otros microservicios (Licitaciones, MS-Ventas,
 * Asistente) resuelvan usuario_id -> {rol, email, nombre} sin duplicar datos.
 * No lo expone el Gateway (no tiene ruta configurada hacia /internal/**);
 * ademas este servicio escucha solo en 127.0.0.1, fuera del alcance de la
 * red local (ver MEMORIA.md, seccion de relajaciones de aislamiento de red).
 */
@RestController
@RequestMapping("/internal/usuarios")
public class InternalUsuarioController {

    private final PerfilService perfilService;

    public InternalUsuarioController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping("/{id}")
    public UsuarioInternoResponse obtener(@PathVariable Long id) {
        return perfilService.obtenerInfoInterna(id);
    }
}
