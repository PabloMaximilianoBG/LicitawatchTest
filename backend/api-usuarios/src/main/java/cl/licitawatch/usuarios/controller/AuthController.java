package cl.licitawatch.usuarios.controller;

import cl.licitawatch.usuarios.dto.LoginRequest;
import cl.licitawatch.usuarios.dto.RefreshRequest;
import cl.licitawatch.usuarios.dto.RegistroClienteRequest;
import cl.licitawatch.usuarios.dto.RegistroEmpresaRequest;
import cl.licitawatch.usuarios.dto.TokenResponse;
import cl.licitawatch.usuarios.dto.UsuarioResumen;
import cl.licitawatch.usuarios.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registro publico: cualquier persona puede registrarse como Empresa o
 * Cliente eligiendo el endpoint correspondiente. El alta de Administrador
 * no vive aqui (ver AdminUsuarioController, restringido a ADMINISTRADOR).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro/empresa")
    public ResponseEntity<UsuarioResumen> registrarEmpresa(@Valid @RequestBody RegistroEmpresaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarEmpresa(req));
    }

    @PostMapping("/registro/cliente")
    public ResponseEntity<UsuarioResumen> registrarCliente(@Valid @RequestBody RegistroClienteRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarCliente(req));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refrescar(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(authService.refrescar(req));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest req) {
        authService.logout(req);
        return ResponseEntity.noContent().build();
    }
}
