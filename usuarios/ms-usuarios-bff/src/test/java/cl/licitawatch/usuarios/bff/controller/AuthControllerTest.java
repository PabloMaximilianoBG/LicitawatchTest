package cl.licitawatch.usuarios.bff.controller;

import cl.licitawatch.common.config.LicitaWatchCommonAutoConfiguration;
import cl.licitawatch.common.error.ErrorResponse;
import cl.licitawatch.common.exception.RemoteServiceException;
import cl.licitawatch.usuarios.bff.dto.response.LoginResponse;
import cl.licitawatch.usuarios.bff.dto.response.PerfilResponse;
import cl.licitawatch.usuarios.bff.service.UsuarioBffService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, UsuarioController.class})
@Import(LicitaWatchCommonAutoConfiguration.class)
@TestPropertySource(properties = "licitawatch.internal.api-key=clave-test")
class AuthControllerTest {
    @Autowired
    MockMvc mvc;
    @MockBean
    UsuarioBffService service;

    @Test
    void login_ok_200() throws Exception {
        PerfilResponse perfil = PerfilResponse.builder().usuarioId(1).email("a@b.cl").rol("PYME").build();
        when(service.login(any())).thenReturn(new LoginResponse("jwt", "Bearer", OffsetDateTime.now(), perfil));
        mvc.perform(post("/api/auth/login").header("X-Internal-Key", "clave-test").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.cl\",\"password\":\"Clave1234\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").value("jwt")).andExpect(jsonPath("$.usuario.rol").value("PYME"));
    }

    @Test
    void login_emailInvalido_400() throws Exception {
        mvc.perform(post("/api/auth/login").header("X-Internal-Key", "clave-test").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"no-es-correo\",\"password\":\"x\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("VALIDACION"));
        verifyNoInteractions(service);
    }

    @Test
    void sinClaveInterna_401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void errorDelBsSePropagaConElMismoCodigo() throws Exception {
        when(service.login(any())).thenThrow(new RemoteServiceException(401,
                ErrorResponse.builder().status(401).codigo("CREDENCIALES_INVALIDAS").mensaje("Correo o contraseña incorrectos").build()));
        mvc.perform(post("/api/auth/login").header("X-Internal-Key", "clave-test").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.cl\",\"password\":\"Clave1234\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
    }
}
