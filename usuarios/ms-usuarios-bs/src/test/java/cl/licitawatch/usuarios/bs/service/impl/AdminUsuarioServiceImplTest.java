package cl.licitawatch.usuarios.bs.service.impl;

import cl.licitawatch.common.exception.BusinessException;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.usuarios.bs.client.UsuarioBdClient;
import cl.licitawatch.usuarios.bs.client.VentasClient;
import cl.licitawatch.usuarios.bs.client.dto.*;
import cl.licitawatch.usuarios.bs.dto.request.CambiarEstadoRequest;
import cl.licitawatch.usuarios.bs.dto.request.CambiarRolRequest;
import cl.licitawatch.usuarios.bs.mapper.PerfilMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUsuarioServiceImplTest {
    @Mock
    UsuarioBdClient bd;
    @Mock
    VentasClient ventas;
    @Mock
    PasswordEncoder encoder;
    @Spy
    PerfilMapper mapper;
    @InjectMocks
    AdminUsuarioServiceImpl service;

    private final UsuarioActual admin = new UsuarioActual(1, "admin@test.cl", "ADMINISTRADOR", 1);

    @Test
    void noPuedeDesactivarseASiMismo() {
        assertThatThrownBy(() -> service.cambiarEstado(admin, 1, new CambiarEstadoRequest(false))).isInstanceOf(BusinessException.class);
    }

    @Test
    void desactivarBloqueaElHashYReactivarLoDesbloquea() {
        when(bd.obtener(5)).thenReturn(new UsuarioBdDto(5, "u@test.cl", "hash", "Licitador", true, LocalDateTime.now()));
        when(bd.perfil(5)).thenReturn(PerfilBdDto.builder().usuarioId(5).rolNombre("Licitador").activo(false).password("!hash").build());

        service.cambiarEstado(admin, 5, new CambiarEstadoRequest(false));
        verify(bd).actualizar(eq(5), argThat(a -> Boolean.FALSE.equals(a.activo()) && "!hash".equals(a.password())));

        when(bd.obtener(5)).thenReturn(new UsuarioBdDto(5, "u@test.cl", "!hash", "Licitador", false, LocalDateTime.now()));
        service.cambiarEstado(admin, 5, new CambiarEstadoRequest(true));
        verify(bd).actualizar(eq(5), argThat(a -> Boolean.TRUE.equals(a.activo()) && "hash".equals(a.password())));
    }

    @Test
    void darAdministrador_creaPerfilAdministradorYCambiaRol() {
        when(bd.obtener(5)).thenReturn(new UsuarioBdDto(5, "u@test.cl", "hash", "Licitador", true, LocalDateTime.now()));
        when(bd.perfiles(5)).thenReturn(new PerfilesUsuarioDto(2, null, null));
        when(bd.perfil(5)).thenReturn(PerfilBdDto.builder().usuarioId(5).rolNombre("Administrador").activo(true).password("hash").build());

        service.cambiarRol(admin, 5, CambiarRolRequest.builder().rol("ADMINISTRADOR").nombre("Juan").area("Soporte").build());

        verify(bd).crearPerfil(eq(5), argThat(c -> "Administrador".equals(c.rolNombre()) && "Juan".equals(c.administrador().nombre())));
        verify(bd).actualizar(eq(5), argThat(a -> "Administrador".equals(a.rolNombre())));
    }
}
