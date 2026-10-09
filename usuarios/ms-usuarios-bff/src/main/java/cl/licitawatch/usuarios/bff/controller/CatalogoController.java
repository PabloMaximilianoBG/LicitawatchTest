package cl.licitawatch.usuarios.bff.controller;

import cl.licitawatch.usuarios.bff.dto.response.CatalogoResponse;
import cl.licitawatch.usuarios.bff.dto.response.CiudadResponse;
import cl.licitawatch.usuarios.bff.service.UsuarioBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Catálogos para los combobox de los formularios (públicos). */
@RestController
@RequestMapping("/api/catalogos")
@RequiredArgsConstructor
public class CatalogoController {
    private final UsuarioBffService service;

    @GetMapping("/rubros")
    public ResponseEntity<List<CatalogoResponse>> rubros() {
        return ResponseEntity.ok(service.rubros());
    }

    @GetMapping("/regiones")
    public ResponseEntity<List<CatalogoResponse>> regiones() {
        return ResponseEntity.ok(service.regiones());
    }

    @GetMapping("/regiones/{id}/ciudades")
    public ResponseEntity<List<CiudadResponse>> ciudades(@PathVariable Integer id) {
        return ResponseEntity.ok(service.ciudades(id));
    }

    @GetMapping("/tamanos-empresa")
    public ResponseEntity<List<CatalogoResponse>> tamanos() {
        return ResponseEntity.ok(service.tamanosEmpresa());
    }
}
