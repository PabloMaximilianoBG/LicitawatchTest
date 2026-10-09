package cl.licitawatch.chat.bff.controller;

import cl.licitawatch.chat.bff.service.ChatBffService;
import cl.licitawatch.common.config.LicitaWatchCommonAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatController.class)
@Import(LicitaWatchCommonAutoConfiguration.class)
@TestPropertySource(properties = "licitawatch.internal.api-key=clave-test")
class ChatControllerTest {
    @Autowired
    MockMvc mvc;
    @MockBean
    ChatBffService service;

    @Test
    void mensajeVacio_400() throws Exception {
        mvc.perform(post("/api/chat/conversaciones/1/mensajes").header("X-Internal-Key", "clave-test")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"contenido\":\"  \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores.contenido").exists());
        verifyNoInteractions(service);
    }
}
