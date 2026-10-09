package cl.licitawatch.asistente.bff.controller;

import cl.licitawatch.asistente.bff.dto.request.ChatRequest;
import cl.licitawatch.asistente.bff.dto.response.ChatResponse;
import cl.licitawatch.asistente.bff.dto.response.EstadoAsistenteResponse;
import cl.licitawatch.asistente.bff.service.AsistenteBffService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/asistente")
@RequiredArgsConstructor
public class AsistenteController {
    private final AsistenteBffService service;

    @Operation(summary = "Acceso a LicitAsist (Pyme: solo Premium) y sugerencias")
    @GetMapping("/estado")
    public ResponseEntity<EstadoAsistenteResponse> estado() {
        return ResponseEntity.ok(service.estado());
    }

    @Operation(summary = "Pregunta a LicitAsist (respuesta con datos reales de la plataforma)")
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(service.chat(request));
    }
}
