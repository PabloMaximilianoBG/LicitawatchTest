package cl.licitawatch.usuarios.bd.controller;

import cl.licitawatch.usuarios.bd.dto.request.RubroRequest;
import cl.licitawatch.usuarios.bd.dto.response.CatalogoResponse;
import cl.licitawatch.usuarios.bd.dto.response.CiudadResponse;
import cl.licitawatch.usuarios.bd.service.CatalogoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bd/catalogos")
@RequiredArgsConstructor
public class CatalogoBdController {
    private final CatalogoService service;

    @GetMapping("/roles")
    public ResponseEntity<List<CatalogoResponse>> roles() {
        return ResponseEntity.ok(service.roles());
    }

    @GetMapping("/rubros")
    public ResponseEntity<List<CatalogoResponse>> rubros() {
        return ResponseEntity.ok(service.rubros());
    }

    @GetMapping("/rubros/{id}")
    public ResponseEntity<CatalogoResponse> rubro(@PathVariable Integer id) {
        return ResponseEntity.ok(service.rubro(id));
    }

    @PostMapping("/rubros")
    public ResponseEntity<CatalogoResponse> crearRubro(@Valid @RequestBody RubroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearRubro(request));
    }

    @GetMapping("/regiones")
    public ResponseEntity<List<CatalogoResponse>> regiones() {
        return ResponseEntity.ok(service.regiones());
    }

    @GetMapping("/regiones/{id}")
    public ResponseEntity<CatalogoResponse> region(@PathVariable Integer id) {
        return ResponseEntity.ok(service.region(id));
    }

    @GetMapping("/regiones/{id}/ciudades")
    public ResponseEntity<List<CiudadResponse>> ciudades(@PathVariable Integer id) {
        return ResponseEntity.ok(service.ciudadesDeRegion(id));
    }

    @GetMapping("/ciudades/{id}")
    public ResponseEntity<CiudadResponse> ciudad(@PathVariable Integer id) {
        return ResponseEntity.ok(service.ciudad(id));
    }

    @GetMapping("/tamanos-empresa")
    public ResponseEntity<List<CatalogoResponse>> tamanos() {
        return ResponseEntity.ok(service.tamanosEmpresa());
    }
}
