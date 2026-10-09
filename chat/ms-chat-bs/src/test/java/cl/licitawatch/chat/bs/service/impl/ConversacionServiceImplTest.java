package cl.licitawatch.chat.bs.service.impl;

import cl.licitawatch.chat.bs.client.ChatBdClient;
import cl.licitawatch.chat.bs.client.LicitacionClient;
import cl.licitawatch.chat.bs.client.NotificacionClient;
import cl.licitawatch.chat.bs.client.UsuarioClient;
import cl.licitawatch.chat.bs.client.dto.*;
import cl.licitawatch.chat.bs.dto.request.MensajeRequest;
import cl.licitawatch.common.error.ErrorResponse;
import cl.licitawatch.common.exception.BusinessException;
import cl.licitawatch.common.exception.ForbiddenException;
import cl.licitawatch.common.exception.RemoteServiceException;
import cl.licitawatch.common.seguridad.UsuarioActual;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversacionServiceImplTest {
    @Mock
    ChatBdClient bd;
    @Mock
    LicitacionClient licitaciones;
    @Mock
    UsuarioClient usuarios;
    @Mock
    NotificacionClient notificaciones;
    @InjectMocks
    ConversacionServiceImpl service;

    private final UsuarioActual licitador = new UsuarioActual(10, "lic@test.cl", "LICITADOR", 3);
    private final UsuarioActual pyme = new UsuarioActual(20, "pyme@test.cl", "PYME", 5);
    private final ConversacionBdDto conversacion = ConversacionBdDto.builder().id(1).postulacionId(9).licitadorId(3).pymeId(5)
            .createdAt(LocalDateTime.now()).build();

    @Test
    void laPymeNoAbreElChat() {
        assertThatThrownBy(() -> service.abrir(pyme, 9)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void elChatSoloSeHabilitaConLaPostulacionAprobada() {
        when(licitaciones.contexto(9)).thenReturn(new ContextoPostulacionDto(9, "Pendiente", 1, "Obra", 3, 5));
        assertThatThrownBy(() -> service.abrir(licitador, 9)).isInstanceOf(BusinessException.class);
        verify(bd, never()).crear(any());
    }

    @Test
    void abrirCreaLaConversacionConLasDosPartes() {
        when(licitaciones.contexto(9)).thenReturn(new ContextoPostulacionDto(9, "Aprobada", 1, "Obra", 3, 5));
        when(bd.porPostulacion(9, 10)).thenThrow(new RemoteServiceException(404, ErrorResponse.builder().status(404).build()));
        when(bd.crear(any())).thenReturn(conversacion);

        service.abrir(licitador, 9);

        verify(bd).crear(new ConversacionBdRequestDto(9, 3, 5));
    }

    @Test
    void unTerceroNoPuedeLeerMensajes() {
        when(bd.obtener(1, null)).thenReturn(conversacion);
        UsuarioActual otraPyme = new UsuarioActual(30, "otra@test.cl", "PYME", 99);
        assertThatThrownBy(() -> service.mensajes(otraPyme, 1, null)).isInstanceOf(ForbiddenException.class);
        verify(bd, never()).mensajes(anyInt(), any());
    }

    @Test
    void enviarAvisaPorCorreoSoloSiLaContraparteNoTeniaPendientes() {
        when(bd.obtener(1, null)).thenReturn(conversacion);
        when(usuarios.licitador(3)).thenReturn(new PerfilDto(10, 3, "Constructora", "Juan", false));
        when(usuarios.pyme(5)).thenReturn(new PerfilDto(20, 5, "Pyme SpA", "Ana", true));
        when(bd.noLeidos(1, 10)).thenReturn(new ConteoDto(0)).thenReturn(new ConteoDto(1));
        when(bd.enviar(eq(1), any())).thenReturn(new MensajeBdDto(1, 1, 20, "Hola", LocalDateTime.now(), false));

        service.enviar(pyme, 1, new MensajeRequest("Hola"));
        service.enviar(pyme, 1, new MensajeRequest("¿Me confirmas?"));

        verify(notificaciones, times(1)).notificar(argThat(n -> n.usuarioId() == 10 && n.tipo().equals("Mensaje nuevo")));
    }
}
