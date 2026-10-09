package cl.licitawatch.licitaciones.bs.service.impl;

import cl.licitawatch.common.exception.BusinessException;
import cl.licitawatch.common.exception.ConflictException;
import cl.licitawatch.common.exception.ForbiddenException;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.licitaciones.bs.client.LicitacionBdClient;
import cl.licitawatch.licitaciones.bs.client.UsuarioClient;
import cl.licitawatch.licitaciones.bs.client.VentasClient;
import cl.licitawatch.licitaciones.bs.client.dto.*;
import cl.licitawatch.licitaciones.bs.dto.request.PostularRequest;
import cl.licitawatch.licitaciones.bs.dto.response.PostulacionResponse;
import cl.licitawatch.licitaciones.bs.mapper.LicitacionMapper;
import cl.licitawatch.licitaciones.bs.service.NotificadorService;
import cl.licitawatch.licitaciones.bs.util.Fechas;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostulacionServiceImplTest {
    @Mock
    LicitacionBdClient bd;
    @Mock
    UsuarioClient usuarios;
    @Mock
    VentasClient ventas;
    @Mock
    NotificadorService notificador;
    @Spy
    LicitacionMapper mapper;
    @InjectMocks
    PostulacionServiceImpl service;

    private final UsuarioActual pyme = new UsuarioActual(20, "pyme@test.cl", "PYME", 5);
    private final UsuarioActual licitador = new UsuarioActual(10, "lic@test.cl", "LICITADOR", 3);

    private LicitacionBdDto licitacion(String estado, LocalDate cierre, Integer max, long cantidad) {
        return LicitacionBdDto.builder().id(1).licitadorId(3).titulo("Obra").estado(estado).fechaCierre(cierre)
                .maxPostulantes(max).cantidadPostulaciones(cantidad).rubroId(1).regionId(1).build();
    }

    @Test
    void noSePuedePostularAUnaCerrada() {
        when(bd.obtener(1)).thenReturn(licitacion("Cerrada", Fechas.hoy().plusDays(3), null, 0));
        assertThatThrownBy(() -> service.postular(pyme, 1, new PostularRequest("hola"))).isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya no recibe");
    }

    @Test
    void noSePuedePostularDosVeces() {
        when(bd.obtener(1)).thenReturn(licitacion("Abierta", Fechas.hoy().plusDays(3), null, 0));
        when(bd.existePostulacion(1, 5)).thenReturn(true);
        assertThatThrownBy(() -> service.postular(pyme, 1, new PostularRequest(null))).isInstanceOf(ConflictException.class);
    }

    @Test
    void sinCuposNoEstaDisponible() {
        when(bd.obtener(1)).thenReturn(licitacion("Abierta", Fechas.hoy().plusDays(3), 2, 2));
        when(bd.existePostulacion(1, 5)).thenReturn(false);
        assertThatThrownBy(() -> service.postular(pyme, 1, new PostularRequest(null))).isInstanceOf(BusinessException.class)
                .hasMessageContaining("máximo");
    }

    @Test
    void estandarNoPuedeSuperar3PostulacionesAlMes() {
        when(bd.obtener(1)).thenReturn(licitacion("Abierta", Fechas.hoy().plusDays(3), null, 0));
        when(bd.existePostulacion(1, 5)).thenReturn(false);
        when(ventas.planVigente(20)).thenReturn(new PlanVigenteDto("Estándar", false, 3));
        when(bd.contarPostulaciones(eq(5), any())).thenReturn(new ConteoDto(3));
        assertThatThrownBy(() -> service.postular(pyme, 1, new PostularRequest(null))).isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("límite de 3");
    }

    @Test
    void postulacionValidaNaceEnPendienteYNotificaAlLicitador() {
        when(bd.obtener(1)).thenReturn(licitacion("Abierta", Fechas.hoy(), null, 0));
        when(bd.existePostulacion(1, 5)).thenReturn(false);
        when(ventas.planVigente(20)).thenReturn(new PlanVigenteDto("Premium", true, 7));
        when(bd.contarPostulaciones(eq(5), any())).thenReturn(new ConteoDto(3));
        when(usuarios.pyme(5)).thenReturn(PerfilDto.builder().perfilId(5).usuarioId(20).razonSocial("Pyme SpA").build());
        when(bd.crearPostulacion(any())).thenReturn(PostulacionBdDto.builder().id(9).licitacionId(1).pymeId(5).estado("Pendiente").build());
        when(usuarios.licitador(3)).thenReturn(PerfilDto.builder().perfilId(3).usuarioId(10).build());

        PostulacionResponse r = service.postular(pyme, 1, new PostularRequest("Propuesta"));

        assertThat(r.estado()).isEqualTo("Pendiente");
        verify(notificador).notificar(eq(10), eq("Postulación recibida"), anyMap());
    }

    @Test
    void soloElDuenoPuedeAprobar() {
        when(bd.postulacion(9)).thenReturn(PostulacionBdDto.builder().id(9).licitacionId(1).pymeId(5).estado("Pendiente").build());
        when(bd.obtener(1)).thenReturn(licitacion("Abierta", Fechas.hoy().plusDays(1), null, 1));
        UsuarioActual otro = new UsuarioActual(11, "otro@test.cl", "LICITADOR", 99);
        assertThatThrownBy(() -> service.aprobar(otro, 9)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void aprobarAdjudicaYNotificaAGanadoraYRechazadas() {
        when(bd.postulacion(9)).thenReturn(PostulacionBdDto.builder().id(9).licitacionId(1).pymeId(5).estado("Pendiente").build());
        when(bd.obtener(1)).thenReturn(licitacion("Cerrada", Fechas.hoy().minusDays(1), null, 2));
        PostulacionBdDto aprobada = PostulacionBdDto.builder().id(9).licitacionId(1).pymeId(5).estado("Aprobada").build();
        PostulacionBdDto rechazada = PostulacionBdDto.builder().id(8).licitacionId(1).pymeId(6).estado("Rechazada").build();
        when(bd.adjudicar(eq(1), any())).thenReturn(new AdjudicacionBdDto(aprobada, List.of(rechazada)));
        when(usuarios.pymes(anyList())).thenReturn(List.of(
                PerfilDto.builder().perfilId(5).usuarioId(20).razonSocial("Ganadora").build(),
                PerfilDto.builder().perfilId(6).usuarioId(21).razonSocial("Otra").build()));

        PostulacionResponse r = service.aprobar(licitador, 9);

        assertThat(r.estado()).isEqualTo("Aprobada");
        assertThat(r.chatDisponible()).isTrue();
        verify(notificador).notificar(eq(20), eq("Postulación aprobada"), anyMap());
        verify(notificador).notificar(eq(21), eq("Postulación rechazada"), argThat(m -> m.get("motivo").contains("adjudicada")));
    }

    @Test
    void postulantesPremiumAparecenPrimero() {
        when(bd.obtener(1)).thenReturn(licitacion("Abierta", Fechas.hoy().plusDays(1), null, 2));
        when(bd.postulacionesDeLicitacion(1)).thenReturn(List.of(
                PostulacionBdDto.builder().id(1).licitacionId(1).pymeId(5).estado("Pendiente").fechaPostulacion(Fechas.hoy().minusDays(2)).build(),
                PostulacionBdDto.builder().id(2).licitacionId(1).pymeId(6).estado("Pendiente").fechaPostulacion(Fechas.hoy()).build()));
        when(usuarios.pymes(anyList())).thenReturn(List.of(
                PerfilDto.builder().perfilId(5).usuarioId(20).premium(false).build(),
                PerfilDto.builder().perfilId(6).usuarioId(21).premium(true).build()));

        List<PostulacionResponse> r = service.postulantes(licitador, 1);

        assertThat(r).extracting(PostulacionResponse::pymeId).containsExactly(6, 5);
        verify(bd, never()).postulaciones(any(), any(), any(), anyInt(), anyInt());
    }
}
