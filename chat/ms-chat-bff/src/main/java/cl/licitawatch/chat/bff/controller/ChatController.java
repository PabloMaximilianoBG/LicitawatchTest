package cl.licitawatch.chat.bff.controller;

import cl.licitawatch.chat.bff.dto.request.AbrirConversacionRequest;
import cl.licitawatch.chat.bff.dto.request.MensajeRequest;
import cl.licitawatch.chat.bff.dto.response.ConversacionResponse;
import cl.licitawatch.chat.bff.dto.response.MensajeResponse;
import cl.licitawatch.chat.bff.service.ChatBffService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat/conversaciones")
@RequiredArgsConstructor
public class ChatController {
    private final ChatBffService service;

    @Operation(summary = "El Licitador abre el chat con la Pyme de una postulación aprobada")
    @PostMapping
    public ResponseEntity<ConversacionResponse> abrir(@Valid @RequestBody AbrirConversacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.abrir(request));
    }

    @GetMapping
    public ResponseEntity<List<ConversacionResponse>> mias() {
        return ResponseEntity.ok(service.mias());
    }

    @GetMapping("/postulacion/{postulacionId}")
    public ResponseEntity<ConversacionResponse> porPostulacion(@PathVariable Integer postulacionId) {
        return ResponseEntity.ok(service.porPostulacion(postulacionId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversacionResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @Operation(summary = "Mensajes (polling: despuesDe = último id recibido)")
    @GetMapping("/{id}/mensajes")
    public ResponseEntity<List<MensajeResponse>> mensajes(@PathVariable Integer id, @RequestParam(required = false) Integer despuesDe) {
        return ResponseEntity.ok(service.mensajes(id, despuesDe));
    }

    @PostMapping("/{id}/mensajes")
    public ResponseEntity<MensajeResponse> enviar(@PathVariable Integer id, @Valid @RequestBody MensajeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.enviar(id, request));
    }
}
