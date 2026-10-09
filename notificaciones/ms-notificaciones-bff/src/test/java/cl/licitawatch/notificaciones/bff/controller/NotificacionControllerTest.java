package cl.licitawatch.notificaciones.bff.controller;

import cl.licitawatch.common.config.LicitaWatchCommonAutoConfiguration;
import cl.licitawatch.notificaciones.bff.service.NotificacionBffService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificacionController.class)
@Import(LicitaWatchCommonAutoConfiguration.class)
@TestPropertySource(properties = "licitawatch.internal.api-key=clave-test")
class NotificacionControllerTest {
    @Autowired
    MockMvc mvc;
    @MockBean
    NotificacionBffService service;

    @Test
    void soporteSinAsunto_400() throws Exception {
        mvc.perform(post("/api/soporte").header("X-Internal-Key", "clave-test").contentType(MediaType.APPLICATION_JSON)
                .content("{\"mensaje\":\"hola\"}")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
