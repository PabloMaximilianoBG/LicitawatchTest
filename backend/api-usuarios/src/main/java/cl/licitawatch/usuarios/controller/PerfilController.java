package cl.licitawatch.usuarios.controller;

import cl.licitawatch.usuarios.dto.ActualizarPerfilClienteRequest;
import cl.licitawatch.usuarios.dto.ActualizarPerfilEmpresaRequest;
import cl.licitawatch.usuarios.dto.PerfilResponse;
import cl.licitawatch.usuarios.security.SecurityUtils;
import cl.licitawatch.usuarios.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Perfil del usuario autenticado. La identidad se toma del JWT (SecurityUtils),
 * nunca de un parametro de la URL: asi cada usuario solo puede leer/editar su
 * propio perfil, sin importar el rol.
 */
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public PerfilResponse obtenerMiPerfil() {
        return perfilService.obtenerPerfil(SecurityUtils.usuarioActual().id());
    }

    @PutMapping("/empresa")
    public PerfilResponse actualizarPerfilEmpresa(@Valid @RequestBody ActualizarPerfilEmpresaRequest req) {
        return perfilService.actualizarPerfilEmpresa(SecurityUtils.usuarioActual().id(), req);
    }

    @PutMapping("/cliente")
    public PerfilResponse actualizarPerfilCliente(@Valid @RequestBody ActualizarPerfilClienteRequest req) {
        return perfilService.actualizarPerfilCliente(SecurityUtils.usuarioActual().id(), req);
    }
}
