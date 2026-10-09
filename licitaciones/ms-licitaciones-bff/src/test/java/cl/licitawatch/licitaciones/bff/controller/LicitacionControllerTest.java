package cl.licitawatch.licitaciones.bff.controller;

import cl.licitawatch.common.config.LicitaWatchCommonAutoConfiguration;
import cl.licitawatch.licitaciones.bff.dto.response.LicitacionResponse;
import cl.licitawatch.licitaciones.bff.service.LicitacionBffService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LicitacionController.class)
@Import(LicitaWatchCommonAutoConfiguration.class)
@TestPropertySource(properties = "licitawatch.internal.api-key=clave-test")
class LicitacionControllerTest {
    @Autowired
    MockMvc mvc;
    @MockBean
    LicitacionBffService service;

    @Test
    void crear_201() throws Exception {
        when(service.crear(any())).thenReturn(LicitacionResponse.builder().id(1).titulo("Obra").estado("Abierta").build());
        mvc.perform(post("/api/licitaciones").header("X-Internal-Key", "clave-test").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Construcción de bodega\",\"descripcion\":\"Descripción suficientemente larga para validar\","
                                + "\"rubroId\":1,\"regionId\":7,\"fechaCierre\":\"" + LocalDate.now().plusDays(10) + "\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("Abierta"));
    }

    @Test
    void crear_sinTitulo_400() throws Exception {
        mvc.perform(post("/api/licitaciones").header("X-Internal-Key", "clave-test").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descripcion\":\"x\",\"rubroId\":1,\"regionId\":7}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores.titulo").exists());
        verifyNoInteractions(service);
    }
}
