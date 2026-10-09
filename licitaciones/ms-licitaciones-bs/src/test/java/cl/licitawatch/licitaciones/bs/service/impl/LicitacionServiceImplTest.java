package cl.licitawatch.licitaciones.bs.service.impl;

import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.common.exception.BusinessException;
import cl.licitawatch.common.exception.ForbiddenException;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.licitaciones.bs.client.ChatClient;
import cl.licitawatch.licitaciones.bs.client.LicitacionBdClient;
import cl.licitawatch.licitaciones.bs.client.UsuarioClient;
import cl.licitawatch.licitaciones.bs.client.dto.EliminacionBdDto;
import cl.licitawatch.licitaciones.bs.client.dto.LicitacionBdDto;
import cl.licitawatch.licitaciones.bs.dto.request.LicitacionRequest;
import cl.licitawatch.licitaciones.bs.mapper.LicitacionMapper;
import cl.licitawatch.licitaciones.bs.service.AlmacenamientoService;
import cl.licitawatch.licitaciones.bs.service.CatalogoNombresService;
import cl.licitawatch.licitaciones.bs.service.NotificadorService;
import cl.licitawatch.licitaciones.bs.util.Fechas;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LicitacionServiceImplTest {
    @Mock
    LicitacionBdClient bd;
    @Mock
    UsuarioClient usuarios;
    @Mock
    ChatClient chat;
    @Mock
    CatalogoNombresService nombres;
    @Mock
    NotificadorService notificador;
    @Mock
    AlmacenamientoService almacenamiento;
    @Spy
    LicitacionMapper mapper;
    @InjectMocks
    LicitacionServiceImpl service;

    private final UsuarioActual licitador = new UsuarioActual(10, "lic@test.cl", "LICITADOR", 3);
    private final UsuarioActual pyme = new UsuarioActual(20, "pyme@test.cl", "PYME", 5);

    private LicitacionRequest request(java.time.LocalDate cierre, BigDecimal min, BigDecimal max) {
        return LicitacionRequest.builder().titulo("Construcción de bodega").descripcion("Descripción suficientemente larga")
                .rubroId(1).regionId(7).presupuestoMin(min).presupuestoMax(max).fechaCierre(cierre).build();
    }

    @Test
    void pymeNoPuedePublicar() {
        assertThatThrownBy(() -> service.crear(pyme, request(Fechas.hoy().plusDays(5), null, null))).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void fechaDeCierreDebeSerFutura() {
        assertThatThrownBy(() -> service.crear(licitador, request(Fechas.hoy(), null, null))).isInstanceOf(BadRequestException.class);
    }

    @Test
    void presupuestoMinimoNoPuedeSuperarAlMaximo() {
        assertThatThrownBy(() -> service.crear(licitador, request(Fechas.hoy().plusDays(5), new BigDecimal("100"), new BigDecimal("50"))))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void soloSeEditanLicitacionesAbiertas() {
        when(bd.obtener(1)).thenReturn(LicitacionBdDto.builder().id(1).licitadorId(3).estado("Adjudicada").fechaCierre(Fechas.hoy()).build());
        assertThatThrownBy(() -> service.actualizar(licitador, 1, request(Fechas.hoy().plusDays(5), null, null)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void eliminarBorraArchivosYConversaciones() {
        when(bd.obtener(1)).thenReturn(LicitacionBdDto.builder().id(1).licitadorId(3).estado("Abierta").fechaCierre(Fechas.hoy()).build());
        when(bd.eliminar(1)).thenReturn(new EliminacionBdDto(1, List.of(4, 5)));

        service.eliminar(licitador, 1);

        verify(almacenamiento).eliminarTodo(1);
        verify(chat).eliminarPorPostulaciones(List.of(4, 5));
    }

    @Test
    void otroLicitadorNoPuedeEliminar() {
        when(bd.obtener(1)).thenReturn(LicitacionBdDto.builder().id(1).licitadorId(99).estado("Abierta").fechaCierre(Fechas.hoy()).build());
        assertThatThrownBy(() -> service.eliminar(licitador, 1)).isInstanceOf(ForbiddenException.class);
        verify(bd, never()).eliminar(any());
    }
}
