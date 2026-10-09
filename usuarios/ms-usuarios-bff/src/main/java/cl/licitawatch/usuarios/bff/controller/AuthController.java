package cl.licitawatch.usuarios.bff.controller;

import cl.licitawatch.usuarios.bff.dto.request.*;
import cl.licitawatch.usuarios.bff.dto.response.LoginResponse;
import cl.licitawatch.usuarios.bff.dto.response.MensajeResponse;
import cl.licitawatch.usuarios.bff.dto.response.RegistroResponse;
import cl.licitawatch.usuarios.bff.service.UsuarioBffService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Rutas públicas de autenticación (el gateway no exige JWT aquí). */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UsuarioBffService service;

    @Operation(summary = "Registro de Licitador (usuario + licitador en una transacción)")
    @PostMapping("/registro/licitador")
    public ResponseEntity<RegistroResponse> registrarLicitador(@Valid @RequestBody RegistroLicitadorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarLicitador(request));
    }

    @Operation(summary = "Registro de Pyme (usuario + pyme; plan Estándar por defecto)")
    @PostMapping("/registro/pyme")
    public ResponseEntity<RegistroResponse> registrarPyme(@Valid @RequestBody RegistroPymeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarPyme(request));
    }

    @Operation(summary = "Inicio de sesión: devuelve el JWT y el perfil completo")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping("/confirmar-cuenta")
    public ResponseEntity<MensajeResponse> confirmarCuenta(@Valid @RequestBody TokenRequest request) {
        return ResponseEntity.ok(service.confirmarCuenta(request));
    }

    @PostMapping("/reenviar-confirmacion")
    public ResponseEntity<MensajeResponse> reenviarConfirmacion(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(service.reenviarConfirmacion(request));
    }

    @Operation(summary = "Envía al correo un enlace para restablecer la contraseña")
    @PostMapping("/recuperar-password")
    public ResponseEntity<MensajeResponse> recuperarPassword(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(service.recuperarPassword(request));
    }

    @PostMapping("/restablecer-password")
    public ResponseEntity<MensajeResponse> restablecerPassword(@Valid @RequestBody RestablecerPasswordRequest request) {
        return ResponseEntity.ok(service.restablecerPassword(request));
    }
}
