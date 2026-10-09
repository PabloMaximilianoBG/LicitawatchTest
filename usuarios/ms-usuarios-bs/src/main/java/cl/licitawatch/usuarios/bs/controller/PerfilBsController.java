package cl.licitawatch.usuarios.bs.controller;

import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.usuarios.bs.dto.request.ActualizarPerfilRequest;
import cl.licitawatch.usuarios.bs.dto.response.PerfilResponse;
import cl.licitawatch.usuarios.bs.dto.response.UsuarioBasicoResponse;
import cl.licitawatch.usuarios.bs.service.PerfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bs")
@RequiredArgsConstructor
public class PerfilBsController {
    private final PerfilService perfilService;

    @GetMapping("/usuarios/me")
    public ResponseEntity<PerfilResponse> me(UsuarioActual usuario) {
        return ResponseEntity.ok(perfilService.miPerfil(usuario));
    }

    @PutMapping("/usuarios/me")
    public ResponseEntity<PerfilResponse> actualizarMe(UsuarioActual usuario, @Valid @RequestBody ActualizarPerfilRequest request) {
        return ResponseEntity.ok(perfilService.actualizarMiPerfil(usuario, request));
    }

    /** Uso interno (Notificaciones, Chat, Ventas): correo y nombre de un usuario. */
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioBasicoResponse> usuario(@PathVariable Integer id) {
        return ResponseEntity.ok(perfilService.usuarioBasico(id));
    }

    @GetMapping("/licitadores/{id}")
    public ResponseEntity<PerfilResponse> licitador(@PathVariable Integer id) {
        return ResponseEntity.ok(perfilService.licitador(id));
    }

    @GetMapping("/licitadores")
    public ResponseEntity<List<PerfilResponse>> licitadores(@RequestParam List<Integer> ids) {
        return ResponseEntity.ok(perfilService.licitadores(ids));
    }

    @GetMapping("/pymes/{id}")
    public ResponseEntity<PerfilResponse> pyme(@PathVariable Integer id) {
        return ResponseEntity.ok(perfilService.pyme(id));
    }

    @GetMapping("/pymes")
    public ResponseEntity<List<PerfilResponse>> pymes(@RequestParam(required = false) List<Integer> ids,
                                                      @RequestParam(required = false) Integer rubroId) {
        if (rubroId != null) {
            return ResponseEntity.ok(perfilService.pymesPorRubro(rubroId));
        }
        return ResponseEntity.ok(ids == null ? List.of() : perfilService.pymes(ids));
    }
}
