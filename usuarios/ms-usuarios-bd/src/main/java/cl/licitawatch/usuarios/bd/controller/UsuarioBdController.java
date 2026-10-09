package cl.licitawatch.usuarios.bd.controller;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.usuarios.bd.dto.request.*;
import cl.licitawatch.usuarios.bd.dto.response.*;
import cl.licitawatch.usuarios.bd.service.UsuarioDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bd")
@RequiredArgsConstructor
public class UsuarioBdController {
    private final UsuarioDataService service;

    @GetMapping("/usuarios")
    public ResponseEntity<PaginaResponse<PerfilBdResponse>> listar(@RequestParam(required = false) String rol,
                                                                   @RequestParam(required = false) Boolean activo,
                                                                   @RequestParam(required = false) String q,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.listar(rol, activo, q, page, size));
    }

    @PostMapping("/usuarios")
    public ResponseEntity<PerfilBdResponse> crear(@Valid @RequestBody CrearUsuarioBdRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioBdResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping("/usuarios/buscar")
    public ResponseEntity<UsuarioBdResponse> porEmail(@RequestParam String email) {
        return ResponseEntity.ok(service.buscarPorEmail(email));
    }

    @GetMapping("/usuarios/existe")
    public ResponseEntity<ExisteResponse> existeEmail(@RequestParam String email) {
        return ResponseEntity.ok(new ExisteResponse(service.existeEmail(email)));
    }

    @PatchMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioBdResponse> actualizar(@PathVariable Integer id, @RequestBody ActualizarUsuarioBdRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @GetMapping("/usuarios/{id}/perfil")
    public ResponseEntity<PerfilBdResponse> perfil(@PathVariable Integer id) {
        return ResponseEntity.ok(service.perfil(id));
    }

    @PostMapping("/usuarios/{id}/perfil")
    public ResponseEntity<PerfilBdResponse> crearPerfil(@PathVariable Integer id, @Valid @RequestBody CrearPerfilBdRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearPerfil(id, request));
    }

    @PutMapping("/usuarios/{id}/perfil")
    public ResponseEntity<PerfilBdResponse> actualizarPerfil(@PathVariable Integer id, @Valid @RequestBody ActualizarPerfilBdRequest request) {
        return ResponseEntity.ok(service.actualizarPerfil(id, request));
    }

    @GetMapping("/usuarios/{id}/perfiles")
    public ResponseEntity<PerfilesUsuarioResponse> perfiles(@PathVariable Integer id) {
        return ResponseEntity.ok(service.perfiles(id));
    }

    @GetMapping("/perfiles/existe-rut")
    public ResponseEntity<ExisteResponse> existeRut(@RequestParam String rut, @RequestParam String rol) {
        return ResponseEntity.ok(new ExisteResponse(service.existeRut(rut, rol)));
    }

    @GetMapping("/licitadores/{id}")
    public ResponseEntity<PerfilBdResponse> licitador(@PathVariable Integer id) {
        return ResponseEntity.ok(service.licitador(id));
    }

    @GetMapping("/licitadores")
    public ResponseEntity<List<PerfilBdResponse>> licitadores(@RequestParam List<Integer> ids) {
        return ResponseEntity.ok(service.licitadores(ids));
    }

    @GetMapping("/pymes/{id}")
    public ResponseEntity<PerfilBdResponse> pyme(@PathVariable Integer id) {
        return ResponseEntity.ok(service.pyme(id));
    }

    @GetMapping("/pymes")
    public ResponseEntity<List<PerfilBdResponse>> pymes(@RequestParam(required = false) List<Integer> ids,
                                                        @RequestParam(required = false) Integer rubroId) {
        if (rubroId != null) {
            return ResponseEntity.ok(service.pymesPorRubro(rubroId));
        }
        return ResponseEntity.ok(ids == null ? List.of() : service.pymes(ids));
    }
}
