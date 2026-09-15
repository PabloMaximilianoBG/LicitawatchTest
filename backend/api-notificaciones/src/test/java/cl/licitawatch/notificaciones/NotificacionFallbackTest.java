package cl.licitawatch.notificaciones;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Sin mocks: API Usuarios no esta corriendo en el puerto de prueba (9998,
 * ver application.yml de test) y el SMTP tampoco es alcanzable. Confirma el
 * requisito central de la seccion 3.4: nada de esto debe romper la
 * respuesta HTTP - la notificacion debe quedar PENDIENTE, nunca un 500.
 */
@SpringBootTest
@AutoConfigureMockMvc
class NotificacionFallbackTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void siUsuariosNoRespondeLaNotificacionQuedaPendienteYNoFalla() throws Exception {
        mockMvc.perform(post("/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "usuarioId", 999,
                                "tipo", "LICITACION",
                                "canal", "EMAIL",
                                "asunto", "Licitacion publicada",
                                "mensaje", "Mensaje de prueba"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado", is("PENDIENTE")));
    }
}
