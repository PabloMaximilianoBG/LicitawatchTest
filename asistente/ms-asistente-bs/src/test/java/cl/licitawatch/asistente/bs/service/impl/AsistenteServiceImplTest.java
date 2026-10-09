package cl.licitawatch.asistente.bs.service.impl;

import cl.licitawatch.asistente.bs.client.GroqClient;
import cl.licitawatch.asistente.bs.client.VentasClient;
import cl.licitawatch.asistente.bs.dto.request.ChatRequest;
import cl.licitawatch.asistente.bs.service.ContextoService;
import cl.licitawatch.common.exception.ForbiddenException;
import cl.licitawatch.common.exception.ServiceUnavailableException;
import cl.licitawatch.common.seguridad.UsuarioActual;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsistenteServiceImplTest {
    @Mock
    VentasClient ventas;
    @Mock
    ContextoService contexto;
    @Mock
    GroqClient groq;

    private final ObjectMapper om = new ObjectMapper();

    private AsistenteServiceImpl service() {
        return new AsistenteServiceImpl(new AccesoServiceImpl(ventas), contexto, groq);
    }

    @Test
    void pymeEstandarNoTieneAcceso() throws Exception {
        when(ventas.planVigente(20)).thenReturn(om.readTree("{\"plan\":\"Estándar\",\"premium\":false}"));
        UsuarioActual pyme = new UsuarioActual(20, "p@test.cl", "PYME", 5);
        assertThatThrownBy(() -> service().chat(pyme, new ChatRequest("hola", null, null)))
                .isInstanceOf(ForbiddenException.class).hasMessageContaining("Premium");
        verifyNoInteractions(groq);
    }

    @Test
    void licitadorTieneAccesoLibreSinConsultarPlan() {
        when(contexto.construir(any(), any())).thenReturn(new ContextoService.Contexto("{}", List.of("Mis licitaciones (0)")));
        when(groq.completar(anyList())).thenReturn("Respuesta");
        UsuarioActual licitador = new UsuarioActual(10, "l@test.cl", "LICITADOR", 3);

        assertThat(service().chat(licitador, new ChatRequest("hola", null, null)).respuesta()).isEqualTo("Respuesta");
        verifyNoInteractions(ventas);
    }

    @Test
    void siNoSePuedeVerificarElPlanNoSeConcedeAcceso() {
        when(ventas.planVigente(20)).thenThrow(new RuntimeException("caído"));
        UsuarioActual pyme = new UsuarioActual(20, "p@test.cl", "PYME", 5);
        assertThatThrownBy(() -> service().chat(pyme, new ChatRequest("hola", null, null))).isInstanceOf(ServiceUnavailableException.class);
    }

    @Test
    void elContextoSoloLlevaCamposPermitidos() throws Exception {
        ContextoServiceImpl ctx = new ContextoServiceImpl(null, null, null, om);
        var filtrado = ctx.filtrar(om.readTree("{\"id\":1,\"pymeRazonSocial\":\"A\",\"pymeEmailContacto\":\"x@y.cl\",\"pymeTelefono\":\"+569\",\"pymeRut\":\"1-9\"}"),
                ContextoServiceImpl.POSTULANTE);
        assertThat(filtrado.has("pymeEmailContacto")).isFalse();
        assertThat(filtrado.has("pymeTelefono")).isFalse();
        assertThat(filtrado.has("pymeRut")).isFalse();
        assertThat(filtrado.get("pymeRazonSocial").asText()).isEqualTo("A");
    }
}
