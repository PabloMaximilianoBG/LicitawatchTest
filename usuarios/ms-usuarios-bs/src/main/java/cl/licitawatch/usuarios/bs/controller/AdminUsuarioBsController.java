package cl.licitawatch.usuarios.bs.controller;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.seguridad.ContextoUsuario;
import cl.licitawatch.common.seguridad.Roles;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.usuarios.bs.dto.request.*;
import cl.licitawatch.usuarios.bs.dto.response.CatalogoResponse;
import cl.licitawatch.usuarios.bs.dto.response.PerfilResponse;
import cl.licitawatch.usuarios.bs.service.AdminUsuarioService;
import cl.licitawatch.usuarios.bs.service.CatalogoService;
import cl.licitawatch.usuarios.bs.service.PerfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Panel de administración (rol ADMINISTRADOR; el gateway y este controller lo exigen). */
@RestController
@RequestMapping("/bs/admin")
@RequiredArgsConstructor
public class AdminUsuarioBsController {
    private final AdminUsuarioService adminService;
    private final PerfilService perfilService;
    private final CatalogoService catalogoService;

    @GetMapping("/usuarios")
    public ResponseEntity<PaginaResponse<PerfilResponse>> listar(@RequestParam(required = false) String rol,
                                                                 @RequestParam(required = false) Boolean activo,
                                                                 @RequestParam(required = false) String q,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        ContextoUsuario.exigirRol(Roles.ADMINISTRADOR);
        return ResponseEntity.ok(adminService.listar(rol, activo, q, page, size));
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<PerfilResponse> obtener(@PathVariable Integer id) {
        ContextoUsuario.exigirRol(Roles.ADMINISTRADOR);
        return ResponseEntity.ok(adminService.obtener(id));
    }

    @PostMapping("/usuarios")
    public ResponseEntity<PerfilResponse> crear(@Valid @RequestBody AdminCrearUsuarioRequest request) {
        ContextoUsuario.exigirRol(Roles.ADMINISTRADOR);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.crear(request));
    }

    @PutMapping("/usuarios/{id}/perfil")
    public ResponseEntity<PerfilResponse> actualizarPerfil(@PathVariable Integer id, @Valid @RequestBody ActualizarPerfilRequest request) {
        ContextoUsuario.exigirRol(Roles.ADMINISTRADOR);
        return ResponseEntity.ok(perfilService.actualizarPerfil(id, request));
    }

    @PatchMapping("/usuarios/{id}/estado")
    public ResponseEntity<PerfilResponse> cambiarEstado(@PathVariable Integer id, @Valid @RequestBody CambiarEstadoRequest request) {
        UsuarioActual admin = ContextoUsuario.exigirRol(Roles.ADMINISTRADOR);
        return ResponseEntity.ok(adminService.cambiarEstado(admin, id, request));
    }

    @PatchMapping("/usuarios/{id}/rol")
    public ResponseEntity<PerfilResponse> cambiarRol(@PathVariable Integer id, @Valid @RequestBody CambiarRolRequest request) {
        UsuarioActual admin = ContextoUsuario.exigirRol(Roles.ADMINISTRADOR);
        return ResponseEntity.ok(adminService.cambiarRol(admin, id, request));
    }

    @PostMapping("/catalogos/rubros")
    public ResponseEntity<CatalogoResponse> crearRubro(@Valid @RequestBody RubroRequest request) {
        ContextoUsuario.exigirRol(Roles.ADMINISTRADOR);
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogoService.crearRubro(request));
    }
}
