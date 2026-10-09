package cl.licitawatch.chat.bs.controller;

import cl.licitawatch.chat.bs.dto.request.AbrirConversacionRequest;
import cl.licitawatch.chat.bs.dto.request.MensajeRequest;
import cl.licitawatch.chat.bs.dto.response.ConversacionResponse;
import cl.licitawatch.chat.bs.dto.response.MensajeResponse;
import cl.licitawatch.chat.bs.service.ConversacionService;
import cl.licitawatch.common.seguridad.UsuarioActual;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bs/conversaciones")
@RequiredArgsConstructor
public class ConversacionBsController {
    private final ConversacionService service;

    @PostMapping
    public ResponseEntity<ConversacionResponse> abrir(UsuarioActual u, @Valid @RequestBody AbrirConversacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.abrir(u, request.postulacionId()));
    }

    @GetMapping
    public ResponseEntity<List<ConversacionResponse>> mias(UsuarioActual u) {
        return ResponseEntity.ok(service.mias(u));
    }

    /** Uso interno (MS.licitaciones.bs al eliminar una licitación). */
    @DeleteMapping
    public ResponseEntity<Void> eliminar(@RequestParam List<Integer> postulacionIds) {
        service.eliminarPorPostulaciones(postulacionIds);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/postulacion/{postulacionId}")
    public ResponseEntity<ConversacionResponse> porPostulacion(UsuarioActual u, @PathVariable Integer postulacionId) {
        return ResponseEntity.ok(service.porPostulacion(u, postulacionId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversacionResponse> obtener(UsuarioActual u, @PathVariable Integer id) {
        return ResponseEntity.ok(service.obtener(u, id));
    }

    @GetMapping("/{id}/mensajes")
    public ResponseEntity<List<MensajeResponse>> mensajes(UsuarioActual u, @PathVariable Integer id,
                                                          @RequestParam(required = false) Integer despuesDe) {
        return ResponseEntity.ok(service.mensajes(u, id, despuesDe));
    }

    @PostMapping("/{id}/mensajes")
    public ResponseEntity<MensajeResponse> enviar(UsuarioActual u, @PathVariable Integer id, @Valid @RequestBody MensajeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.enviar(u, id, request));
    }
}
