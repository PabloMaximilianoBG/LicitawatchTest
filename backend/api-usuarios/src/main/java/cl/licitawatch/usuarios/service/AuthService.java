package cl.licitawatch.usuarios.service;

import cl.licitawatch.usuarios.dto.LoginRequest;
import cl.licitawatch.usuarios.dto.RefreshRequest;
import cl.licitawatch.usuarios.dto.RegistroClienteRequest;
import cl.licitawatch.usuarios.dto.RegistroEmpresaRequest;
import cl.licitawatch.usuarios.dto.TokenResponse;
import cl.licitawatch.usuarios.dto.UsuarioResumen;
import cl.licitawatch.usuarios.entity.Cliente;
import cl.licitawatch.usuarios.entity.Empresa;
import cl.licitawatch.usuarios.entity.RefreshToken;
import cl.licitawatch.usuarios.entity.RolNombre;
import cl.licitawatch.usuarios.entity.Usuario;
import cl.licitawatch.usuarios.exception.CredencialesInvalidasException;
import cl.licitawatch.usuarios.exception.EmailYaRegistradoException;
import cl.licitawatch.usuarios.exception.RutYaRegistradoException;
import cl.licitawatch.usuarios.exception.TokenInvalidoException;
import cl.licitawatch.usuarios.repository.ClienteRepository;
import cl.licitawatch.usuarios.repository.EmpresaRepository;
import cl.licitawatch.usuarios.repository.RefreshTokenRepository;
import cl.licitawatch.usuarios.repository.RolRepository;
import cl.licitawatch.usuarios.repository.UsuarioRepository;
import cl.licitawatch.usuarios.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final RolRepository rolRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshExpirationDays;

    public AuthService(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            ClienteRepository clienteRepository,
            RolRepository rolRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${jwt.refresh-expiration-days}") long refreshExpirationDays) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.clienteRepository = clienteRepository;
        this.rolRepository = rolRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshExpirationDays = refreshExpirationDays;
    }

    @Transactional
    public UsuarioResumen registrarEmpresa(RegistroEmpresaRequest req) {
        validarEmailYRutDisponibles(req.email(), req.rut(), empresaRepository.existsByRut(req.rut()));

        Usuario usuario = crearUsuarioBase(req.email(), req.contrasena(), RolNombre.EMPRESA);
        usuarioRepository.save(usuario);

        Empresa empresa = new Empresa();
        empresa.setUsuario(usuario);
        empresa.setRazonSocial(req.razonSocial());
        empresa.setRut(req.rut());
        empresa.setRubro(req.rubro());
        empresaRepository.save(empresa);

        return new UsuarioResumen(usuario.getId(), usuario.getEmail(), RolNombre.EMPRESA.name());
    }

    @Transactional
    public UsuarioResumen registrarCliente(RegistroClienteRequest req) {
        validarEmailYRutDisponibles(req.email(), req.rut(), clienteRepository.existsByRut(req.rut()));

        Usuario usuario = crearUsuarioBase(req.email(), req.contrasena(), RolNombre.CLIENTE);
        usuarioRepository.save(usuario);

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setNombreContacto(req.nombreContacto());
        cliente.setRut(req.rut());
        clienteRepository.save(cliente);

        return new UsuarioResumen(usuario.getId(), usuario.getEmail(), RolNombre.CLIENTE.name());
    }

    @Transactional
    public TokenResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByEmail(req.email())
                .filter(Usuario::isActivo)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!passwordEncoder.matches(req.contrasena(), usuario.getContrasenaHash())) {
            throw new CredencialesInvalidasException();
        }

        return emitirTokens(usuario);
    }

    @Transactional
    public TokenResponse refrescar(RefreshRequest req) {
        String hash = hashToken(req.refreshToken());
        RefreshToken almacenado = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new TokenInvalidoException("Refresh token invalido"));

        if (almacenado.isRevocado() || almacenado.getExpiraEn().isBefore(LocalDateTime.now())) {
            throw new TokenInvalidoException("Refresh token invalido o expirado");
        }

        almacenado.setRevocado(true);
        refreshTokenRepository.save(almacenado);

        return emitirTokens(almacenado.getUsuario());
    }

    @Transactional
    public void logout(RefreshRequest req) {
        String hash = hashToken(req.refreshToken());
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.setRevocado(true);
            refreshTokenRepository.save(token);
        });
    }

    private TokenResponse emitirTokens(Usuario usuario) {
        String accessToken = jwtService.generarAccessToken(usuario);
        String refreshTokenPlano = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsuario(usuario);
        refreshToken.setTokenHash(hashToken(refreshTokenPlano));
        refreshToken.setExpiraEn(LocalDateTime.now().plusDays(refreshExpirationDays));
        refreshTokenRepository.save(refreshToken);

        UsuarioResumen resumen = new UsuarioResumen(usuario.getId(), usuario.getEmail(), usuario.getRol().getNombre().name());
        return new TokenResponse(accessToken, refreshTokenPlano, jwtService.getExpiracionSegundos(), resumen);
    }

    private Usuario crearUsuarioBase(String email, String contrasena, RolNombre rolNombre) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setContrasenaHash(passwordEncoder.encode(contrasena));
        usuario.setRol(rolRepository.findByNombre(rolNombre).orElseThrow());
        usuario.setActivo(true);
        return usuario;
    }

    private void validarEmailYRutDisponibles(String email, String rut, boolean rutYaExiste) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailYaRegistradoException(email);
        }
        if (rutYaExiste) {
            throw new RutYaRegistradoException(rut);
        }
    }

    private String hashToken(String tokenPlano) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(tokenPlano.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
        }
    }
}
