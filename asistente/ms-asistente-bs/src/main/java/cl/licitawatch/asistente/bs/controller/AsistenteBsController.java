package cl.licitawatch.asistente.bs.controller;

import cl.licitawatch.asistente.bs.dto.request.ChatRequest;
import cl.licitawatch.asistente.bs.dto.response.ChatResponse;
import cl.licitawatch.asistente.bs.dto.response.EstadoAsistenteResponse;
import cl.licitawatch.asistente.bs.service.AsistenteService;
import cl.licitawatch.common.seguridad.UsuarioActual;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bs/asistente")
@RequiredArgsConstructor
public class AsistenteBsController {
    private final AsistenteService service;

    @GetMapping("/estado")
    public ResponseEntity<EstadoAsistenteResponse> estado(UsuarioActual u) {
        return ResponseEntity.ok(service.estado(u));
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(UsuarioActual u, @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(service.chat(u, request));
    }
}
