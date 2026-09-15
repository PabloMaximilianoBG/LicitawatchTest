package cl.licitawatch.asistente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.licitawatch.asistente.exception.LimiteGroqException;
import cl.licitawatch.asistente.groq.GroqClient;
import cl.licitawatch.asistente.groq.GroqMensaje;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ChatFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GroqClient groqClient;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String token(Long usuarioId, String email, String rol) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Date ahora = new Date();
        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                .claim("email", email)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + 3_600_000))
                .signWith(key)
                .compact();
    }

    @Test
    void empresaPuedeChatearYElUsoSeIncrementaPorCadaMensaje() throws Exception {
        when(groqClient.responder(anyList())).thenReturn("Hola, soy LicitAsist. ¿En que te ayudo?");
        String token = token(400L, "empresa@test.cl", "EMPRESA");

        mockMvc.perform(post("/api/asistente/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("mensaje", "¿Cuantas licitaciones tengo activas?"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuesta").exists())
                .andExpect(jsonPath("$.usoDelUsuario", is(1)));

        mockMvc.perform(post("/api/asistente/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("mensaje", "¿Y postulaciones recibidas?"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usoDelUsuario", is(2)));

        mockMvc.perform(get("/api/asistente/uso").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usoDelUsuario", is(2)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void elHistorialDeConversacionSeEnviaEnElSegundoMensaje() throws Exception {
        when(groqClient.responder(anyList())).thenReturn("respuesta 1", "respuesta 2");
        String token = token(401L, "cliente@test.cl", "CLIENTE");

        mockMvc.perform(post("/api/asistente/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("mensaje", "primer mensaje"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/asistente/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("mensaje", "segundo mensaje"))))
                .andExpect(status().isOk());

        ArgumentCaptor<List<GroqMensaje>> captor = ArgumentCaptor.forClass(List.class);
        verify(groqClient, org.mockito.Mockito.times(2)).responder(captor.capture());

        List<GroqMensaje> mensajesSegundaLlamada = captor.getAllValues().get(1);
        boolean incluyeHistorialPrimerTurno = mensajesSegundaLlamada.stream()
                .anyMatch(m -> "primer mensaje".equals(m.content()));
        assertThat(incluyeHistorialPrimerTurno).isTrue();
    }

    @Test
    void unAdministradorNoPuedeUsarElChat() throws Exception {
        String token = token(1L, "admin@test.cl", "ADMINISTRADOR");
        mockMvc.perform(post("/api/asistente/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("mensaje", "hola"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/asistente/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("mensaje", "hola"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cuerpoConJsonMalformadoDevuelve400YNo500() throws Exception {
        mockMvc.perform(post("/api/asistente/chat")
                        .header("Authorization", "Bearer " + token(403L, "empresa3@test.cl", "EMPRESA"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ esto no es json valido"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cuandoGroqAlcanzaElLimiteSeDevuelve429ConMensajeClaro() throws Exception {
        when(groqClient.responder(anyList())).thenThrow(new LimiteGroqException());
        String token = token(402L, "empresa2@test.cl", "EMPRESA");

        mockMvc.perform(post("/api/asistente/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("mensaje", "hola"))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.mensaje").exists());
    }
}
