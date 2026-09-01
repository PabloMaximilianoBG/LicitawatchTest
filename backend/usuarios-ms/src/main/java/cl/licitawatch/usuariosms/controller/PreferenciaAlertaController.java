package cl.licitawatch.usuariosms.controller;

import cl.licitawatch.usuariosms.entity.PreferenciaAlerta;
import cl.licitawatch.usuariosms.service.PreferenciaAlertaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/preferencias")
@RequiredArgsConstructor
public class PreferenciaAlertaController {

    private final PreferenciaAlertaService preferenciaAlertaService;

    @GetMapping
    public List<PreferenciaAlerta> listar(@PathVariable Long usuarioId) {
        return preferenciaAlertaService.listarPorUsuario(usuarioId);
    }

    @PostMapping
    public ResponseEntity<PreferenciaAlerta> crear(@PathVariable Long usuarioId,
                                                     @Valid @RequestBody PreferenciaAlerta preferencia) {
        return ResponseEntity.status(201).body(preferenciaAlertaService.crear(usuarioId, preferencia));
    }

    @PutMapping("/{id}")
    public PreferenciaAlerta actualizar(@PathVariable Long usuarioId, @PathVariable Long id,
                                         @Valid @RequestBody PreferenciaAlerta cambios) {
        return preferenciaAlertaService.actualizar(id, cambios);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long usuarioId, @PathVariable Long id) {
        preferenciaAlertaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
