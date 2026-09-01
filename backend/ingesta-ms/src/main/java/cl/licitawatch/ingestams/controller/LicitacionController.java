package cl.licitawatch.ingestams.controller;

import cl.licitawatch.ingestams.entity.Licitacion;
import cl.licitawatch.ingestams.service.LicitacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/licitaciones")
@RequiredArgsConstructor
public class LicitacionController {

    private final LicitacionService licitacionService;

    @GetMapping
    public List<Licitacion> listar() {
        return licitacionService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Licitacion> buscar(@PathVariable Long id) {
        Licitacion licitacion = licitacionService.buscar(id);
        return licitacion == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(licitacion);
    }

    @PostMapping
    public ResponseEntity<Licitacion> crear(@Valid @RequestBody Licitacion licitacion) {
        return ResponseEntity.status(201).body(licitacionService.crear(licitacion));
    }
}
