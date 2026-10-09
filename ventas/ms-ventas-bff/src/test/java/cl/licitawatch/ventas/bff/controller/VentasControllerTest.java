package cl.licitawatch.ventas.bff.controller;

import cl.licitawatch.common.config.LicitaWatchCommonAutoConfiguration;
import cl.licitawatch.ventas.bff.dto.request.RetornoWebpayRequest;
import cl.licitawatch.ventas.bff.service.VentasBffService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VentasController.class)
@Import(LicitaWatchCommonAutoConfiguration.class)
@TestPropertySource(properties = "licitawatch.internal.api-key=clave-test")
class VentasControllerTest {
    @Autowired
    MockMvc mvc;
    @MockBean
    VentasBffService service;

    @Test
    void retornoWebpayRedirigeAlFrontend() throws Exception {
        when(service.retornoWebpay(new RetornoWebpayRequest("tok", null, null, null)))
                .thenReturn("http://localhost:5173/pago/resultado?estado=aprobado&venta=12");
        mvc.perform(post("/api/pagos/webpay/retorno").header("X-Internal-Key", "clave-test").param("token_ws", "tok"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:5173/pago/resultado?estado=aprobado&venta=12"));
    }
}
