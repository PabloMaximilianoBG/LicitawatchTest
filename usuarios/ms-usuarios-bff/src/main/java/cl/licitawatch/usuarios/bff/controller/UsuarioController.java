package cl.licitawatch.usuarios.bff.controller;

import cl.licitawatch.usuarios.bff.dto.request.ActualizarPerfilRequest;
import cl.licitawatch.usuarios.bff.dto.response.PerfilPublicoResponse;
import cl.licitawatch.usuarios.bff.dto.response.PerfilResponse;
import cl.licitawatch.usuarios.bff.service.UsuarioBffService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioBffService service;

    @Operation(summary = "Usuario logueado + perfil completo con catálogos resueltos")
    @GetMapping("/me")
    public ResponseEntity<PerfilResponse> me() {
        return ResponseEntity.ok(service.me());
    }

    @Operation(summary = "Edición del perfil (sin RUT ni correo de acceso); actualiza updated_at")
    @PutMapping("/me")
    public ResponseEntity<PerfilResponse> actualizarMe(@Valid @RequestBody ActualizarPerfilRequest request) {
        return ResponseEntity.ok(service.actualizarMe(request));
    }

    @GetMapping("/licitadores/{id}")
    public ResponseEntity<PerfilPublicoResponse> licitador(@PathVariable Integer id) {
        return ResponseEntity.ok(service.licitador(id));
    }

    @Operation(summary = "Perfil de una Pyme (con insignia Premium)")
    @GetMapping("/pymes/{id}")
    public ResponseEntity<PerfilPublicoResponse> pyme(@PathVariable Integer id) {
        return ResponseEntity.ok(service.pyme(id));
    }
}
