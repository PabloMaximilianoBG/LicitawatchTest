package cl.licitawatch.usuarios;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
 * Prueba de integracion del flujo completo de API Usuarios sobre H2 en
 * memoria (ver src/test/resources/application.yml): registro -> login ->
 * perfil propio -> refresh -> logout, mas los casos de error mas relevantes
 * (email duplicado, credenciales invalidas, RBAC de administracion).
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void flujoCompletoDeRegistroLoginYPerfilParaEmpresa() throws Exception {
        Map<String, String> registro = Map.of(
                "email", "empresa1@licitawatch.cl",
                "contrasena", "ClaveSegura123",
                "razonSocial", "Constructora Andes SpA",
                "rut", "76123456-7",
                "rubro", "Construccion"
        );

        mockMvc.perform(post("/api/auth/registro/empresa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registro)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol", is("EMPRESA")));

        // Email duplicado debe rechazarse
        mockMvc.perform(post("/api/auth/registro/empresa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registro)))
                .andExpect(status().isConflict());

        Map<String, String> login = Map.of(
                "email", "empresa1@licitawatch.cl",
                "contrasena", "ClaveSegura123"
        );

        String loginResponseJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        Map<String, Object> loginResponse = objectMapper.readValue(loginResponseJson, Map.class);
        String accessToken = (String) loginResponse.get("accessToken");
        String refreshToken = (String) loginResponse.get("refreshToken");

        mockMvc.perform(get("/api/perfil")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("empresa1@licitawatch.cl")))
                .andExpect(jsonPath("$.razonSocial", is("Constructora Andes SpA")));

        mockMvc.perform(put("/api/perfil/empresa")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "razonSocial", "Constructora Andes Ltda",
                                "rubro", "Construccion e Ingenieria"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razonSocial", is("Constructora Andes Ltda")));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());

        // El refresh token ya se roto en el paso anterior: reusarlo debe fallar
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginConCredencialesInvalidasDevuelve401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "no-existe@licitawatch.cl",
                                "contrasena", "cualquiera123"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void perfilSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/perfil"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rutasDeAdminSonRechazadasParaUnClienteSinRolAdministrador() throws Exception {
        Map<String, String> registro = Map.of(
                "email", "cliente1@licitawatch.cl",
                "contrasena", "ClaveSegura123",
                "nombreContacto", "Juan Perez",
                "rut", "12345678-9"
        );
        mockMvc.perform(post("/api/auth/registro/cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registro)))
                .andExpect(status().isCreated());

        String loginJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "cliente1@licitawatch.cl",
                                "contrasena", "ClaveSegura123"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        Map<String, Object> loginResponse = objectMapper.readValue(loginJson, Map.class);
        String accessToken = (String) loginResponse.get("accessToken");

        mockMvc.perform(get("/api/admin/usuarios")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void cuerpoConJsonMalformadoDevuelve400YNo500() throws Exception {
        // Regresion: un JSON invalido (comillas sin escapar, etc.) lanza
        // HttpMessageNotReadableException, que antes caia en el handler
        // generico de Exception.class y devolvia 500 en vez de 400.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{email: esto no es json valido}"))
                .andExpect(status().isBadRequest());
    }
}
