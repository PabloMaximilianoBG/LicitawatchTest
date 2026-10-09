package cl.licitawatch.usuarios.bs.controller;

import cl.licitawatch.usuarios.bs.dto.request.*;
import cl.licitawatch.usuarios.bs.dto.response.LoginResponse;
import cl.licitawatch.usuarios.bs.dto.response.MensajeResponse;
import cl.licitawatch.usuarios.bs.dto.response.RegistroResponse;
import cl.licitawatch.usuarios.bs.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bs/auth")
@RequiredArgsConstructor
public class AuthBsController {
    private final AuthService authService;

    @PostMapping("/registro/licitador")
    public ResponseEntity<RegistroResponse> registrarLicitador(@Valid @RequestBody RegistroLicitadorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarLicitador(request));
    }

    @PostMapping("/registro/pyme")
    public ResponseEntity<RegistroResponse> registrarPyme(@Valid @RequestBody RegistroPymeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarPyme(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/confirmar-cuenta")
    public ResponseEntity<MensajeResponse> confirmar(@Valid @RequestBody TokenRequest request) {
        return ResponseEntity.ok(authService.confirmarCuenta(request));
    }

    @PostMapping("/reenviar-confirmacion")
    public ResponseEntity<MensajeResponse> reenviar(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(authService.reenviarConfirmacion(request));
    }

    @PostMapping("/recuperar-password")
    public ResponseEntity<MensajeResponse> recuperar(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(authService.recuperarPassword(request));
    }

    @PostMapping("/restablecer-password")
    public ResponseEntity<MensajeResponse> restablecer(@Valid @RequestBody RestablecerPasswordRequest request) {
        return ResponseEntity.ok(authService.restablecerPassword(request));
    }
}
