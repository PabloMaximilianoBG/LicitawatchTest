package cl.licitawatch.usuarios.controller;

import cl.licitawatch.usuarios.dto.ActualizarActivoRequest;
import cl.licitawatch.usuarios.dto.CrearAdministradorRequest;
import cl.licitawatch.usuarios.dto.PerfilResponse;
import cl.licitawatch.usuarios.dto.UsuarioAdminResponse;
import cl.licitawatch.usuarios.dto.UsuarioResumen;
import cl.licitawatch.usuarios.service.AdminUsuarioService;
import cl.licitawatch.usuarios.service.PerfilService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Todo bajo /api/admin/** ya exige rol ADMINISTRADOR a nivel de SecurityConfig;
 * el @PreAuthorize se repite aqui como segunda capa explicita por endpoint.
 */
@RestController
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminUsuarioController {

    private final AdminUsuarioService adminUsuarioService;
    private final PerfilService perfilService;

    public AdminUsuarioController(AdminUsuarioService adminUsuarioService, PerfilService perfilService) {
        this.adminUsuarioService = adminUsuarioService;
        this.perfilService = perfilService;
    }

    @GetMapping
    public List<UsuarioAdminResponse> listar() {
        return adminUsuarioService.listarUsuarios();
    }

    @GetMapping("/{id}")
    public PerfilResponse obtener(@PathVariable Long id) {
        return perfilService.obtenerPerfil(id);
    }

    @PutMapping("/{id}/activo")
    public ResponseEntity<Void> actualizarActivo(@PathVariable Long id, @Valid @RequestBody ActualizarActivoRequest req) {
        adminUsuarioService.actualizarActivo(id, req.activo());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/administradores")
    public ResponseEntity<UsuarioResumen> crearAdministrador(@Valid @RequestBody CrearAdministradorRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUsuarioService.crearAdministrador(req));
    }
}
