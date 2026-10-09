package cl.licitawatch.usuarios.bs.service.impl;

import cl.licitawatch.common.error.ErrorResponse;
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
import cl.licitawatch.usuarios.bs.dto.response.LoginResponse;
import cl.licitawatch.usuarios.bs.dto.response.RegistroResponse;
import cl.licitawatch.usuarios.bs.mapper.PerfilMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    private static final String SECRET = "x".repeat(64);

    @Mock
    UsuarioBdClient bd;
    @Mock
    VentasClient ventas;
    @Mock
    NotificacionClient notificaciones;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final TokenServiceImpl tokens = new TokenServiceImpl(SECRET, 60);
    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(bd, ventas, notificaciones, tokens, encoder, new PerfilMapper());
        ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:5173");
    }

    private RegistroPymeRequest pyme(String password, String confirm) {
        return RegistroPymeRequest.builder().email("pyme@test.cl").password(password).confirmPassword(confirm)
                .razonSocial("Pyme SpA").rut("76.543.210-3").nombreContacto("Ana").emailContacto("ana@test.cl")
                .telefono("+56911112222").rubroId(1).ciudadId(1).tamanoEmpresaId(1).descripcionEmpresa("Servicios").build();
    }

    private PerfilBdDto perfilPyme(boolean activo, String hash) {
        return PerfilBdDto.builder().usuarioId(7).email("pyme@test.cl").password(hash).rolNombre("Pyme").activo(activo)
                .createdAt(LocalDateTime.now()).perfilId(3).razonSocial("Pyme SpA").nombreContacto("Ana").build();
    }

    @Test
    void registroPyme_rechazaContrasenasDistintas() {
        assertThatThrownBy(() -> service.registrarPyme(pyme("Clave1234", "Clave9999")))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("no coinciden");
        verifyNoInteractions(bd);
    }

    @Test
    void registroPyme_rechazaRutDuplicado() {
        when(bd.existeEmail(anyString())).thenReturn(new ExisteDto(false));
        when(bd.existeRut("76543210-3", "Pyme")).thenReturn(new ExisteDto(true));
        assertThatThrownBy(() -> service.registrarPyme(pyme("Clave1234", "Clave1234"))).isInstanceOf(ConflictException.class);
    }

    @Test
    void registroPyme_creaInactivaAsignaEstandarYEnviaConfirmacion() {
        when(bd.existeEmail(anyString())).thenReturn(new ExisteDto(false));
        when(bd.existeRut(anyString(), anyString())).thenReturn(new ExisteDto(false));
        when(bd.crear(any())).thenReturn(perfilPyme(false, "hash"));

        RegistroResponse r = service.registrarPyme(pyme("Clave1234", "Clave1234"));

        ArgumentCaptor<CrearUsuarioBdDto> captor = ArgumentCaptor.forClass(CrearUsuarioBdDto.class);
        verify(bd).crear(captor.capture());
        assertThat(captor.getValue().activo()).isFalse();
        assertThat(captor.getValue().rolNombre()).isEqualTo("Pyme");
        assertThat(captor.getValue().empresa().rut()).isEqualTo("76543210-3");
        assertThat(encoder.matches("Clave1234", captor.getValue().password())).isTrue();
        verify(ventas).asignarEstandar(new UsuarioIdDto(7));
        verify(notificaciones).confirmacionCuenta(any());
        assertThat(r.correoEnviado()).isTrue();
        assertThat(r.usuario().estadoCuenta()).isEqualTo("PENDIENTE_CONFIRMACION");
    }

    @Test
    void login_cuentaNoConfirmada_403() {
        String hash = encoder.encode("Clave1234");
        when(bd.buscarPorEmail("pyme@test.cl")).thenReturn(new UsuarioBdDto(7, "pyme@test.cl", hash, "Pyme", false, LocalDateTime.now()));
        assertThatThrownBy(() -> service.login(new LoginRequest("pyme@test.cl", "Clave1234")))
                .isInstanceOf(ForbiddenException.class).hasMessageContaining("confirmar");
    }

    @Test
    void login_cuentaDesactivada_403() {
        String hash = "!" + encoder.encode("Clave1234");
        when(bd.buscarPorEmail("pyme@test.cl")).thenReturn(new UsuarioBdDto(7, "pyme@test.cl", hash, "Pyme", false, LocalDateTime.now()));
        assertThatThrownBy(() -> service.login(new LoginRequest("pyme@test.cl", "Clave1234")))
                .isInstanceOf(ForbiddenException.class).hasMessageContaining("desactivada");
    }

    @Test
    void login_correoInexistente_401() {
        when(bd.buscarPorEmail(anyString())).thenThrow(new RemoteServiceException(404, ErrorResponse.builder().status(404).build()));
        assertThatThrownBy(() -> service.login(new LoginRequest("nadie@test.cl", "Clave1234"))).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void login_ok_emiteJwtConPremium() {
        String hash = encoder.encode("Clave1234");
        when(bd.buscarPorEmail("pyme@test.cl")).thenReturn(new UsuarioBdDto(7, "pyme@test.cl", hash, "Pyme", true, LocalDateTime.now()));
        when(bd.perfil(7)).thenReturn(perfilPyme(true, hash));
        when(ventas.usuariosPremium(List.of(7))).thenReturn(List.of(7));

        LoginResponse r = service.login(new LoginRequest("pyme@test.cl", "Clave1234"));

        assertThat(r.token()).isNotBlank();
        assertThat(r.usuario().rol()).isEqualTo("PYME");
        assertThat(r.usuario().premium()).isTrue();
    }

    @Test
    void confirmarCuenta_activaUsuarioPendiente() {
        String token = tokens.emitirConfirmacionCuenta(7);
        when(bd.obtener(7)).thenReturn(new UsuarioBdDto(7, "pyme@test.cl", "hash", "Pyme", false, LocalDateTime.now()));

        service.confirmarCuenta(new TokenRequest(token));

        verify(bd).actualizar(eq(7), argThat(a -> Boolean.TRUE.equals(a.activo())));
    }

    @Test
    void restablecerPassword_tokenSoloSirveUnaVez() {
        String hashOriginal = encoder.encode("Original1");
        String token = tokens.emitirRestablecerPassword(7, hashOriginal);
        when(bd.obtener(7)).thenReturn(new UsuarioBdDto(7, "p@test.cl", "otroHash", "Pyme", true, LocalDateTime.now()));

        assertThatThrownBy(() -> service.restablecerPassword(new RestablecerPasswordRequest(token, "Nueva1234", "Nueva1234")))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("utilizado");
    }

    @Test
    void tokenDeCorreo_noEsValidoComoConfirmacionSiEsDeOtroProposito() {
        String reset = tokens.emitirRestablecerPassword(7, "hash");
        assertThatThrownBy(() -> service.confirmarCuenta(new TokenRequest(reset))).isInstanceOf(BadRequestException.class);
    }
}
