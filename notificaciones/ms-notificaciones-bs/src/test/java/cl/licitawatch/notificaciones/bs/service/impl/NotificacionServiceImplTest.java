package cl.licitawatch.notificaciones.bs.service.impl;

import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.notificaciones.bs.client.NotificacionBdClient;
import cl.licitawatch.notificaciones.bs.client.UsuarioClient;
import cl.licitawatch.notificaciones.bs.client.VentasClient;
import cl.licitawatch.notificaciones.bs.client.dto.*;
import cl.licitawatch.notificaciones.bs.dto.request.NotificacionRequest;
import cl.licitawatch.notificaciones.bs.dto.request.SoporteRequest;
import cl.licitawatch.notificaciones.bs.dto.response.NotificacionResponse;
import cl.licitawatch.notificaciones.bs.dto.response.SoporteResponse;
import cl.licitawatch.notificaciones.bs.service.CorreoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceImplTest {
    @Mock
    NotificacionBdClient bd;
    @Mock
    UsuarioClient usuarios;
    @Mock
    VentasClient ventas;
    @Mock
    CorreoService correo;

    private NotificacionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificacionServiceImpl(bd, usuarios, ventas, correo, new ContenidoCorreoFactoryImpl("http://localhost:5173"));
        ReflectionTestUtils.setField(service, "casillaSoporte", "soporte@licitawatch.cl");
    }

    private void tipos() {
        when(bd.tipos()).thenReturn(List.of(new CatalogoDto(1, "Licitación publicada"), new CatalogoDto(5, "Pago confirmado")));
    }

    @Test
    void tipoFueraDelCatalogoEsRechazado() {
        tipos();
        assertThatThrownBy(() -> service.notificar(new NotificacionRequest(1, "General", Map.of()))).isInstanceOf(BadRequestException.class);
        verifyNoInteractions(correo);
    }

    @Test
    void enviaCorreoYRegistraConCanalEmail() {
        tipos();
        when(usuarios.usuario(7)).thenReturn(new UsuarioDto(7, "pyme@test.cl", "PYME", true, "Pyme SpA", 3));
        when(bd.registrar(any())).thenReturn(new NotificacionBdDto(1, 7, "Pago confirmado", "email", LocalDateTime.now()));

        NotificacionResponse r = service.notificar(new NotificacionRequest(7, "Pago confirmado", Map.of("plan", "Premium", "monto", "$24.990")));

        verify(correo).enviar(eq("pyme@test.cl"), argThat(c -> c.asunto().contains("Premium") && "$24.990".equals(c.montoDestacado())));
        verify(bd).registrar(new NotificacionBdRequestDto(7, "Pago confirmado", "email"));
        assertThat(r.correoEnviado()).isTrue();
    }

    @Test
    void siFallaElSmtpIgualQuedaElRegistro() {
        tipos();
        when(usuarios.usuario(7)).thenReturn(new UsuarioDto(7, "pyme@test.cl", "PYME", true, "Pyme SpA", 3));
        doThrow(new RuntimeException("smtp caído")).when(correo).enviar(anyString(), any());
        when(bd.registrar(any())).thenReturn(new NotificacionBdDto(1, 7, "Licitación publicada", "email", LocalDateTime.now()));

        assertThat(service.notificar(new NotificacionRequest(7, "Licitación publicada", Map.of())).correoEnviado()).isFalse();
    }

    @Test
    void soportePrioritarioParaPymePremium() {
        UsuarioActual pyme = new UsuarioActual(7, "pyme@test.cl", "PYME", 3);
        when(usuarios.usuario(7)).thenReturn(new UsuarioDto(7, "pyme@test.cl", "PYME", true, "Pyme SpA", 3));
        when(ventas.planVigente(7)).thenReturn(new PlanVigenteDto("Premium", true, 7));

        SoporteResponse r = service.soporte(pyme, new SoporteRequest("Problema", "No puedo subir un archivo"));

        assertThat(r.nivel()).isEqualTo("Prioritario");
        verify(correo).enviarSoporte(eq("soporte@licitawatch.cl"), eq("pyme@test.cl"), eq(true),
                argThat(c -> c.asunto().startsWith("[Soporte prioritario]")));
    }

    @Test
    void soporteEstandarParaLicitador() {
        UsuarioActual licitador = new UsuarioActual(9, "lic@test.cl", "LICITADOR", 2);
        when(usuarios.usuario(9)).thenReturn(new UsuarioDto(9, "lic@test.cl", "LICITADOR", true, "Constructora", 2));

        assertThat(service.soporte(licitador, new SoporteRequest("Duda", "¿Cómo cierro una licitación?")).nivel()).isEqualTo("Estándar");
        verifyNoInteractions(ventas);
    }
}
