package cl.licitawatch.ventas.bs.service.impl;

import cl.licitawatch.common.exception.ConflictException;
import cl.licitawatch.common.exception.ForbiddenException;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.ventas.bs.client.NotificacionClient;
import cl.licitawatch.ventas.bs.client.PasarelaClient;
import cl.licitawatch.ventas.bs.client.VentasBdClient;
import cl.licitawatch.ventas.bs.client.dto.*;
import cl.licitawatch.ventas.bs.config.PlanesProperties;
import cl.licitawatch.ventas.bs.dto.request.RetornoWebpayRequest;
import cl.licitawatch.ventas.bs.dto.response.IniciarPagoResponse;
import cl.licitawatch.ventas.bs.dto.response.PlanVigenteResponse;
import cl.licitawatch.ventas.bs.dto.response.RetornoResponse;
import cl.licitawatch.ventas.bs.mapper.VentasMapper;
import cl.licitawatch.ventas.bs.service.ClienteService;
import cl.licitawatch.ventas.bs.service.SuscripcionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceImplTest {
    @Mock
    VentasBdClient bd;
    @Mock
    PasarelaClient pasarela;
    @Mock
    NotificacionClient notificaciones;
    @Mock
    SuscripcionService suscripciones;
    @Mock
    ClienteService clienteService;

    private PagoServiceImpl service;
    private final UsuarioActual pyme = new UsuarioActual(20, "pyme@test.cl", "PYME", 5);
    private final PlanesProperties planes = new PlanesProperties(new PlanesProperties.Estandar(3, List.of()),
            new PlanesProperties.Premium(new BigDecimal("24990"), 30, 7, List.of()));

    @BeforeEach
    void setUp() {
        service = new PagoServiceImpl(bd, pasarela, notificaciones, suscripciones, clienteService, planes, new VentasMapper());
        ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:5173");
        ReflectionTestUtils.setField(service, "returnUrl", "http://localhost:8080/api/pagos/webpay/retorno");
    }

    @Test
    void elLicitadorNoPaga() {
        UsuarioActual licitador = new UsuarioActual(10, "l@test.cl", "LICITADOR", 3);
        assertThatThrownBy(() -> service.iniciarPremium(licitador)).isInstanceOf(ForbiddenException.class);
        verifyNoInteractions(bd, pasarela);
    }

    @Test
    void noSeCompraPremiumSiYaEstaVigente() {
        when(suscripciones.planVigente(20)).thenReturn(new PlanVigenteResponse("Premium", true, 7));
        assertThatThrownBy(() -> service.iniciarPremium(pyme)).isInstanceOf(ConflictException.class);
    }

    @Test
    void iniciarCreaSuscripcionCanceladaYVentaSinPago() {
        when(suscripciones.planVigente(20)).thenReturn(new PlanVigenteResponse("Estándar", false, 3));
        when(bd.ventasPendientesPremium(20)).thenReturn(List.of());
        when(bd.crearSuscripcion(any())).thenReturn(SuscripcionBdDto.builder().id(4).usuarioId(20).plan("Premium").estado("Cancelada").build());
        when(bd.crearVenta(any())).thenReturn(VentaBdDto.builder().id(12).suscripcionId(4).usuarioId(20).monto(new BigDecimal("24990")).build());
        when(pasarela.crear(any())).thenReturn(new TransaccionDto("tok", "https://webpay3gint.transbank.cl/webpayserver/initTransaction"));

        IniciarPagoResponse r = service.iniciarPremium(pyme);

        assertThat(r.ventaId()).isEqualTo(12);
        verify(bd).crearSuscripcion(new SuscripcionBdRequestDto(20, "Premium", "Cancelada"));
        verify(pasarela).crear(argThat(c -> c.ordenCompra().startsWith("LW-V12-") && c.ordenCompra().length() <= 26));
        verify(bd, never()).registrarPago(anyInt(), any());
    }

    @Test
    void retornoAprobadoRegistraPagoActivaPremiumYNotifica() {
        when(pasarela.confirmar("tok")).thenReturn(ResultadoPagoDto.builder().aprobado(true).monto(new BigDecimal("24990"))
                .ordenCompra("LW-V12-261007120000").metodoPago("Débito").codigoAutorizacion("1213").build());
        when(bd.venta(12)).thenReturn(VentaBdDto.builder().id(12).usuarioId(20).plan("Premium").monto(new BigDecimal("24990"))
                .fecha(LocalDate.now()).build());
        when(bd.registrarPago(eq(12), any())).thenReturn(VentaBdDto.builder().id(12).usuarioId(20).plan("Premium")
                .monto(new BigDecimal("24990")).fecha(LocalDate.now()).build());

        RetornoResponse r = service.procesarRetorno(new RetornoWebpayRequest("tok", null, null, null));

        assertThat(r.redirectUrl()).contains("estado=aprobado").contains("venta=12");
        verify(bd).registrarPago(12, new PagoBdRequestDto("tok", "Débito", "Aprobado", true));
        verify(notificaciones).notificar(argThat(n -> n.tipo().equals("Pago confirmado") && n.usuarioId() == 20));
    }

    @Test
    void retornoConMontoAlteradoSeRechaza() {
        when(pasarela.confirmar("tok")).thenReturn(ResultadoPagoDto.builder().aprobado(true).monto(new BigDecimal("100"))
                .ordenCompra("LW-V12-261007120000").metodoPago("Crédito").build());
        when(bd.venta(12)).thenReturn(VentaBdDto.builder().id(12).usuarioId(20).monto(new BigDecimal("24990")).build());

        RetornoResponse r = service.procesarRetorno(new RetornoWebpayRequest("tok", null, null, null));

        assertThat(r.redirectUrl()).contains("estado=rechazado");
        verify(bd).registrarPago(12, new PagoBdRequestDto("tok", "Crédito", "Rechazado", false));
        verifyNoInteractions(notificaciones);
    }

    @Test
    void pagoAnuladoDejaLaVentaPendienteSinCrearPago() {
        RetornoResponse r = service.procesarRetorno(new RetornoWebpayRequest(null, "tbk", "LW-V12-261007120000", "U20"));
        assertThat(r.redirectUrl()).contains("estado=anulado").contains("venta=12");
        verifyNoInteractions(bd, pasarela);
    }

    @Test
    void retornoRepetidoEsIdempotente() {
        when(pasarela.confirmar("tok")).thenReturn(ResultadoPagoDto.builder().aprobado(true).monto(new BigDecimal("24990"))
                .ordenCompra("LW-V12-261007120000").build());
        when(bd.venta(12)).thenReturn(VentaBdDto.builder().id(12).monto(new BigDecimal("24990"))
                .pago(new PagoBdDto(1, "tok", "Débito", "Aprobado")).build());

        assertThat(service.procesarRetorno(new RetornoWebpayRequest("tok", null, null, null)).redirectUrl()).contains("aprobado");
        verify(bd, never()).registrarPago(anyInt(), any());
    }
}
