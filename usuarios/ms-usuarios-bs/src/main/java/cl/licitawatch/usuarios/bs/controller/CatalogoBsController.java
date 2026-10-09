package cl.licitawatch.usuarios.bs.controller;

import cl.licitawatch.usuarios.bs.dto.response.CatalogoResponse;
import cl.licitawatch.usuarios.bs.dto.response.CiudadResponse;
import cl.licitawatch.usuarios.bs.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bs/catalogos")
@RequiredArgsConstructor
public class CatalogoBsController {
    private final CatalogoService catalogoService;

    @GetMapping("/rubros")
    public ResponseEntity<List<CatalogoResponse>> rubros() {
        return ResponseEntity.ok(catalogoService.rubros());
    }

    @GetMapping("/rubros/{id}")
    public ResponseEntity<CatalogoResponse> rubro(@PathVariable Integer id) {
        return ResponseEntity.ok(catalogoService.rubro(id));
    }

    @GetMapping("/regiones")
    public ResponseEntity<List<CatalogoResponse>> regiones() {
        return ResponseEntity.ok(catalogoService.regiones());
    }

    @GetMapping("/regiones/{id}")
    public ResponseEntity<CatalogoResponse> region(@PathVariable Integer id) {
        return ResponseEntity.ok(catalogoService.region(id));
    }

    @GetMapping("/regiones/{id}/ciudades")
    public ResponseEntity<List<CiudadResponse>> ciudades(@PathVariable Integer id) {
        return ResponseEntity.ok(catalogoService.ciudades(id));
    }

    @GetMapping("/tamanos-empresa")
    public ResponseEntity<List<CatalogoResponse>> tamanos() {
        return ResponseEntity.ok(catalogoService.tamanosEmpresa());
    }
}
