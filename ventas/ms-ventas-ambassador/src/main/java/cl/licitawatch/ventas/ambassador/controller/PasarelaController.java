package cl.licitawatch.ventas.ambassador.controller;

import cl.licitawatch.ventas.ambassador.dto.request.CrearTransaccionRequest;
import cl.licitawatch.ventas.ambassador.dto.response.ResultadoPagoResponse;
import cl.licitawatch.ventas.ambassador.dto.response.TransaccionResponse;
import cl.licitawatch.ventas.ambassador.service.PasarelaPagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pasarela/transacciones")
@RequiredArgsConstructor
public class PasarelaController {
    private final PasarelaPagoService pasarela;

    @PostMapping
    public ResponseEntity<TransaccionResponse> crear(@Valid @RequestBody CrearTransaccionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pasarela.crearTransaccion(request));
    }

    @PutMapping("/{token}")
    public ResponseEntity<ResultadoPagoResponse> confirmar(@PathVariable String token) {
        return ResponseEntity.ok(pasarela.confirmar(token));
    }

    @GetMapping("/{token}")
    public ResponseEntity<ResultadoPagoResponse> estado(@PathVariable String token) {
        return ResponseEntity.ok(pasarela.estado(token));
    }
}
