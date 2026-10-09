package cl.licitawatch.usuarios.bs.service.impl;

import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.common.exception.ConflictException;
import cl.licitawatch.common.exception.ForbiddenException;
import cl.licitawatch.common.exception.RemoteServiceException;
import cl.licitawatch.common.exception.UnauthorizedException;
import cl.licitawatch.usuarios.bs.client.NotificacionClient;
import cl.licitawatch.usuarios.bs.client.UsuarioBdClient;
import cl.licitawatch.usuarios.bs.client.VentasClient;
import cl.licitawatch.usuarios.bs.client.dto.*;
import cl.licitawatch.usuarios.bs.dto.request.*;
import cl.licitawatch.usuarios.bs.dto.response.*;
import cl.licitawatch.usuarios.bs.mapper.PerfilMapper;
import cl.licitawatch.usuarios.bs.service.AuthService;
import cl.licitawatch.usuarios.bs.service.TokenService;
import cl.licitawatch.usuarios.bs.util.EstadoCuenta;
import cl.licitawatch.usuarios.bs.util.RutUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Registro (PPT: 3 perfiles; ER pág. 7: usuario + perfil en una transacción), login JWT,
 * confirmación de cuenta y recuperación de contraseña por correo (sin tablas extra: tokens firmados).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final String RESPUESTA_GENERICA = "Si el correo está registrado, te enviamos un mensaje con las instrucciones.";
    /** Hash ficticio para igualar el tiempo de respuesta cuando el correo no existe (anti-enumeración). */
    private static final String HASH_FICTICIO = "$2a$12$C6UzMDM.H6dfI/f/IKcEeO6s3x9Qh1f6Yq8C7K1QzF4N9cR4Gm3K2";

    private final UsuarioBdClient bd;
    private final VentasClient ventas;
    private final NotificacionClient notificaciones;
    private final TokenService tokens;
    private final PasswordEncoder passwordEncoder;
    private final PerfilMapper mapper;

    @Value("${licitawatch.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public RegistroResponse registrarLicitador(RegistroLicitadorRequest r) {
        String rut = validarComun(r.email(), r.password(), r.confirmPassword(), r.rut(), "Licitador");
        DatosEmpresaBdDto empresa = DatosEmpresaBdDto.builder().razonSocial(r.razonSocial().trim()).rut(rut)
                .nombreContacto(r.nombreContacto()).emailContacto(r.emailContacto()).telefono(r.telefono())
                .rubroId(r.rubroId()).ciudadId(r.ciudadId()).descripcionEmpresa(r.descripcionEmpresa()).sitioWeb(r.sitioWeb()).build();
        PerfilBdDto creado = bd.crear(CrearUsuarioBdDto.builder().email(r.email()).password(passwordEncoder.encode(r.password()))
                .rolNombre("Licitador").activo(false).empresa(empresa).build());
        return completarRegistro(creado);
    }

    @Override
    public RegistroResponse registrarPyme(RegistroPymeRequest r) {
        String rut = validarComun(r.email(), r.password(), r.confirmPassword(), r.rut(), "Pyme");
        DatosEmpresaBdDto empresa = DatosEmpresaBdDto.builder().razonSocial(r.razonSocial().trim()).rut(rut)
                .nombreContacto(r.nombreContacto()).emailContacto(r.emailContacto()).telefono(r.telefono())
                .rubroId(r.rubroId()).ciudadId(r.ciudadId()).tamanoEmpresaId(r.tamanoEmpresaId())
                .descripcionEmpresa(r.descripcionEmpresa()).sitioWeb(r.sitioWeb()).build();
        PerfilBdDto creado = bd.crear(CrearUsuarioBdDto.builder().email(r.email()).password(passwordEncoder.encode(r.password()))
                .rolNombre("Pyme").activo(false).empresa(empresa).build());
        // PPT diap. 7: el plan Estándar viene por defecto (gratis)
        try {
            ventas.asignarEstandar(new UsuarioIdDto(creado.usuarioId()));
        } catch (Exception e) {
            log.warn("No se pudo asignar el plan Estándar al usuario {} (se asignará al consultar su plan): {}", creado.usuarioId(), e.getMessage());
        }
        return completarRegistro(creado);
    }

    @Override
    public LoginResponse login(LoginRequest r) {
        UsuarioBdDto u;
        try {
            u = bd.buscarPorEmail(r.email().trim());
        } catch (RemoteServiceException e) {
            if (e.esNoEncontrado()) {
                passwordEncoder.matches(r.password(), HASH_FICTICIO);
                throw new UnauthorizedException("CREDENCIALES_INVALIDAS", "Correo o contraseña incorrectos");
            }
            throw e;
        }
        if (!passwordEncoder.matches(r.password(), EstadoCuenta.desbloquear(u.password()))) {
            throw new UnauthorizedException("CREDENCIALES_INVALIDAS", "Correo o contraseña incorrectos");
        }
        switch (EstadoCuenta.de(u.activo(), u.password())) {
            case DESACTIVADA -> throw new ForbiddenException("CUENTA_DESACTIVADA",
                    "Tu cuenta fue desactivada por el administrador. Contacta a soporte.");
            case PENDIENTE_CONFIRMACION -> throw new ForbiddenException("CUENTA_NO_CONFIRMADA",
                    "Debes confirmar tu correo antes de iniciar sesión. Revisa tu bandeja de entrada.");
            default -> {
            }
        }
        PerfilResponse perfil = mapper.perfil(bd.perfil(u.id()), esPremium(u.id(), u.rolNombre()));
        TokenService.TokenAcceso t = tokens.emitirAcceso(perfil);
        return new LoginResponse(t.token(), "Bearer", t.expiraEn(), perfil);
    }

    @Override
    public MensajeResponse confirmarCuenta(TokenRequest r) {
        Integer usuarioId = tokens.verificarConfirmacionCuenta(r.token());
        UsuarioBdDto u = bd.obtener(usuarioId);
        EstadoCuenta estado = EstadoCuenta.de(u.activo(), u.password());
        if (estado == EstadoCuenta.ACTIVA) {
            return new MensajeResponse("Tu cuenta ya estaba confirmada. Ya puedes iniciar sesión.");
        }
        if (estado == EstadoCuenta.DESACTIVADA) {
            throw new ForbiddenException("CUENTA_DESACTIVADA", "Esta cuenta fue desactivada por el administrador.");
        }
        bd.actualizar(usuarioId, ActualizarUsuarioBdDto.builder().activo(true).build());
        return new MensajeResponse("¡Cuenta confirmada! Ya puedes iniciar sesión.");
    }

    @Override
    public MensajeResponse reenviarConfirmacion(EmailRequest r) {
        try {
            UsuarioBdDto u = bd.buscarPorEmail(r.email().trim());
            if (EstadoCuenta.de(u.activo(), u.password()) == EstadoCuenta.PENDIENTE_CONFIRMACION) {
                enviarConfirmacion(bd.perfil(u.id()));
            }
        } catch (RemoteServiceException e) {
            if (!e.esNoEncontrado()) {
                throw e;
            }
        }
        return new MensajeResponse(RESPUESTA_GENERICA);
    }

    @Override
    public MensajeResponse recuperarPassword(EmailRequest r) {
        try {
            UsuarioBdDto u = bd.buscarPorEmail(r.email().trim());
            if (EstadoCuenta.de(u.activo(), u.password()) != EstadoCuenta.DESACTIVADA) {
                PerfilBdDto p = bd.perfil(u.id());
                String enlace = frontendUrl + "/restablecer-password?token="
                        + URLEncoder.encode(tokens.emitirRestablecerPassword(u.id(), u.password()), StandardCharsets.UTF_8);
                try {
                    notificaciones.restablecerPassword(new CorreoEnlaceDto(u.email(), nombreDe(p), enlace));
                } catch (Exception e) {
                    log.error("No se pudo enviar el correo de recuperación a {}: {}", u.email(), e.getMessage());
                }
            }
        } catch (RemoteServiceException e) {
            if (!e.esNoEncontrado()) {
                throw e;
            }
        }
        return new MensajeResponse(RESPUESTA_GENERICA);
    }

    @Override
    public MensajeResponse restablecerPassword(RestablecerPasswordRequest r) {
        validarPassword(r.password(), r.confirmPassword());
        Integer usuarioId = tokens.verificarRestablecerPassword(r.token(), id -> bd.obtener(id).password());
        UsuarioBdDto u = bd.obtener(usuarioId);
        if (EstadoCuenta.de(u.activo(), u.password()) == EstadoCuenta.DESACTIVADA) {
            throw new ForbiddenException("CUENTA_DESACTIVADA", "Esta cuenta fue desactivada por el administrador.");
        }
        // Recibir el correo demuestra que el correo es suyo: si la cuenta estaba pendiente, queda confirmada.
        bd.actualizar(usuarioId, ActualizarUsuarioBdDto.builder().password(passwordEncoder.encode(r.password())).activo(true).build());
        return new MensajeResponse("Tu contraseña fue actualizada. Ya puedes iniciar sesión.");
    }

    // ------------------------------------------------------------------ apoyo

    private String validarComun(String email, String password, String confirm, String rut, String rolNombre) {
        validarPassword(password, confirm);
        if (!RutUtils.esValido(rut)) {
            throw new BadRequestException("VALIDACION", "RUT inválido (formato 12345678-9 con dígito verificador correcto)");
        }
        String normalizado = RutUtils.normalizar(rut);
        if (bd.existeEmail(email.trim()).existe()) {
            throw new ConflictException("EMAIL_DUPLICADO", "Ya existe una cuenta con ese correo");
        }
        if (bd.existeRut(normalizado, rolNombre).existe()) {
            throw new ConflictException("RUT_DUPLICADO", "Ya existe una empresa registrada con ese RUT");
        }
        return normalizado;
    }

    static void validarPassword(String password, String confirm) {
        if (!password.equals(confirm)) {
            throw new BadRequestException("PASSWORDS_NO_COINCIDEN", "Las contraseñas no coinciden");
        }
        if (!password.matches(".*[A-Za-zÁÉÍÓÚáéíóúÑñ].*") || !password.matches(".*\\d.*")) {
            throw new BadRequestException("PASSWORD_DEBIL", "La contraseña debe tener al menos una letra y un número");
        }
    }

    private RegistroResponse completarRegistro(PerfilBdDto creado) {
        boolean enviado = enviarConfirmacion(creado);
        String mensaje = enviado
                ? "Cuenta creada. Te enviamos un correo a " + creado.email() + " para confirmar tu cuenta."
                : "Cuenta creada, pero no pudimos enviar el correo de confirmación. Usa \"Reenviar confirmación\".";
        return new RegistroResponse(mapper.perfil(creado, false), enviado, mensaje);
    }

    private boolean enviarConfirmacion(PerfilBdDto p) {
        String enlace = frontendUrl + "/confirmar-cuenta?token="
                + URLEncoder.encode(tokens.emitirConfirmacionCuenta(p.usuarioId()), StandardCharsets.UTF_8);
        try {
            notificaciones.confirmacionCuenta(new CorreoEnlaceDto(p.email(), nombreDe(p), enlace));
            return true;
        } catch (Exception e) {
            log.error("No se pudo enviar el correo de confirmación a {}: {}", p.email(), e.getMessage());
            return false;
        }
    }

    private static String nombreDe(PerfilBdDto p) {
        return p.nombreContacto() != null ? p.nombreContacto() : p.nombre() != null ? p.nombre() : p.email();
    }

    private boolean esPremium(Integer usuarioId, String rolNombre) {
        if (!"Pyme".equalsIgnoreCase(rolNombre)) {
            return false;
        }
        try {
            List<Integer> premium = ventas.usuariosPremium(List.of(usuarioId));
            return premium != null && premium.contains(usuarioId);
        } catch (Exception e) {
            log.warn("No se pudo consultar el plan del usuario {}: {}", usuarioId, e.getMessage());
            return false;
        }
    }
}
