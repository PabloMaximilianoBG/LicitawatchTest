package cl.licitawatch.usuarios.bff.controller;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.usuarios.bff.dto.request.*;
import cl.licitawatch.usuarios.bff.dto.response.CatalogoResponse;
import cl.licitawatch.usuarios.bff.dto.response.PerfilResponse;
import cl.licitawatch.usuarios.bff.service.UsuarioBffService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUsuarioController {
    private final UsuarioBffService service;

    @GetMapping("/usuarios")
    public ResponseEntity<PaginaResponse<PerfilResponse>> listar(@RequestParam(required = false) String rol,
                                                                 @RequestParam(required = false) Boolean activo,
                                                                 @RequestParam(required = false) String q,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.adminListar(rol, activo, q, page, size));
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<PerfilResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.adminObtener(id));
    }

    @Operation(summary = "Crear una cuenta de cualquier rol (por ejemplo, otro Administrador)")
    @PostMapping("/usuarios")
    public ResponseEntity<PerfilResponse> crear(@Valid @RequestBody AdminCrearUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.adminCrear(request));
    }

    @PutMapping("/usuarios/{id}/perfil")
    public ResponseEntity<PerfilResponse> actualizarPerfil(@PathVariable Integer id, @Valid @RequestBody ActualizarPerfilRequest request) {
        return ResponseEntity.ok(service.adminActualizarPerfil(id, request));
    }

    @Operation(summary = "Activar / desactivar una cuenta")
    @PatchMapping("/usuarios/{id}/estado")
    public ResponseEntity<PerfilResponse> cambiarEstado(@PathVariable Integer id, @Valid @RequestBody CambiarEstadoRequest request) {
        return ResponseEntity.ok(service.adminCambiarEstado(id, request));
    }

    @Operation(summary = "Cambiar el rol (ej: dar Administrador a otra persona)")
    @PatchMapping("/usuarios/{id}/rol")
    public ResponseEntity<PerfilResponse> cambiarRol(@PathVariable Integer id, @Valid @RequestBody CambiarRolRequest request) {
        return ResponseEntity.ok(service.adminCambiarRol(id, request));
    }

    @PostMapping("/catalogos/rubros")
    public ResponseEntity<CatalogoResponse> crearRubro(@Valid @RequestBody RubroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.adminCrearRubro(request));
    }
}
