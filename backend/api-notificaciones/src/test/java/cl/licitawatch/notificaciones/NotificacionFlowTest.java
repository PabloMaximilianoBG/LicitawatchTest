package cl.licitawatch.notificaciones;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.licitawatch.notificaciones.dto.UsuarioInternoResponse;
import cl.licitawatch.notificaciones.service.EmailService;
import cl.licitawatch.notificaciones.service.UsuarioClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Camino feliz: se simula que API Usuarios SI responde y el envio de correo
 * SI funciona, para validar que la notificacion queda en estado ENVIADO.
 * El envio real de SMTP y la llamada real a Usuarios se prueban por separado
 * en NotificacionFallbackTest, sin mocks.
 */
@SpringBootTest
@AutoConfigureMockMvc
class NotificacionFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioClient usuarioClient;

    @MockBean
    private EmailService emailService;

    @Test
    void notificacionConUsuarioResueltoYCorreoEnviadoQuedaEnviada() throws Exception {
        when(usuarioClient.resolver(anyLong()))
                .thenReturn(Optional.of(new UsuarioInternoResponse(1L, "empresa@test.cl", "EMPRESA", "Empresa Test", true)));
        when(emailService.enviar(anyString(), anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "usuarioId", 1,
                                "tipo", "LICITACION",
                                "canal", "EMAIL",
                                "asunto", "Licitacion publicada",
                                "mensaje", "Tu licitacion fue publicada correctamente."))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado", is("ENVIADO")));
    }

    @Test
    void siElEnvioDeCorreoFallaLaNotificacionQuedaPendiente() throws Exception {
        when(usuarioClient.resolver(anyLong()))
                .thenReturn(Optional.of(new UsuarioInternoResponse(2L, "cliente@test.cl", "CLIENTE", "Cliente Test", true)));
        when(emailService.enviar(anyString(), anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "usuarioId", 2,
                                "tipo", "PAGO",
                                "canal", "EMAIL",
                                "asunto", "Pago confirmado",
                                "mensaje", "Tu pago fue aprobado."))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado", is("PENDIENTE")));
    }

    @Test
    void cuerpoInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post("/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("tipo", "LICITACION"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cuerpoConJsonMalformadoDevuelve400YNo500() throws Exception {
        mockMvc.perform(post("/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ esto no es json valido"))
                .andExpect(status().isBadRequest());
    }
}
