package cl.licitawatch.chat.bd.controller;

import cl.licitawatch.chat.bd.dto.request.ConversacionBdRequest;
import cl.licitawatch.chat.bd.dto.request.MensajeBdRequest;
import cl.licitawatch.chat.bd.dto.response.ConteoResponse;
import cl.licitawatch.chat.bd.dto.response.ConversacionBdResponse;
import cl.licitawatch.chat.bd.dto.response.MensajeBdResponse;
import cl.licitawatch.chat.bd.service.ChatDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bd/conversaciones")
@RequiredArgsConstructor
public class ChatBdController {
    private final ChatDataService service;

    @PostMapping
    public ResponseEntity<ConversacionBdResponse> crear(@Valid @RequestBody ConversacionBdRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<ConversacionBdResponse>> listar(@RequestParam(required = false) Integer licitadorId,
                                                               @RequestParam(required = false) Integer pymeId,
                                                               @RequestParam(required = false) Integer lectorId) {
        return ResponseEntity.ok(service.listar(licitadorId, pymeId, lectorId));
    }

    @DeleteMapping
    public ResponseEntity<ConteoResponse> eliminar(@RequestParam List<Integer> postulacionIds) {
        return ResponseEntity.ok(new ConteoResponse(service.eliminarPorPostulaciones(postulacionIds)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversacionBdResponse> obtener(@PathVariable Integer id, @RequestParam(required = false) Integer lectorId) {
        return ResponseEntity.ok(service.obtener(id, lectorId));
    }

    @GetMapping("/postulacion/{postulacionId}")
    public ResponseEntity<ConversacionBdResponse> porPostulacion(@PathVariable Integer postulacionId,
                                                                 @RequestParam(required = false) Integer lectorId) {
        return ResponseEntity.ok(service.porPostulacion(postulacionId, lectorId));
    }

    @GetMapping("/{id}/mensajes")
    public ResponseEntity<List<MensajeBdResponse>> mensajes(@PathVariable Integer id, @RequestParam(required = false) Integer despuesDeId) {
        return ResponseEntity.ok(service.mensajes(id, despuesDeId));
    }

    @PostMapping("/{id}/mensajes")
    public ResponseEntity<MensajeBdResponse> enviar(@PathVariable Integer id, @Valid @RequestBody MensajeBdRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.enviar(id, request));
    }

    @PatchMapping("/{id}/leidos")
    public ResponseEntity<ConteoResponse> leidos(@PathVariable Integer id, @RequestParam Integer lectorId) {
        return ResponseEntity.ok(new ConteoResponse(service.marcarLeidos(id, lectorId)));
    }

    @GetMapping("/{id}/no-leidos")
    public ResponseEntity<ConteoResponse> noLeidos(@PathVariable Integer id, @RequestParam Integer lectorId) {
        return ResponseEntity.ok(new ConteoResponse(service.noLeidos(id, lectorId)));
    }
}
