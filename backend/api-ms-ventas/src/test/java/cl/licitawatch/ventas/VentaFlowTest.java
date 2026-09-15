package cl.licitawatch.ventas;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(FakePasarelaPagoAdapter.class)
class VentaFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FakePasarelaPagoAdapter fakePasarela;

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
    void catalogoDePlanesTieneEstandarYPremium() throws Exception {
        mockMvc.perform(get("/api/planes").header("Authorization", "Bearer " + token(1L, "a@test.cl", "CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.nombre=='ESTANDAR')].precio", org.hamcrest.Matchers.contains(0.0)));
    }

    @Test
    void suscripcionPorDefectoEsEstandarGratisYNoRequierePago() throws Exception {
        String tokenUsuario = token(999L, "nuevo@test.cl", "EMPRESA");

        // Un usuario que nunca ha comprado nada ya tiene Estandar activo, sin pagar.
        mockMvc.perform(get("/api/suscripciones/mia").header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan", is("ESTANDAR")))
                .andExpect(jsonPath("$.estado", is("ACTIVA")));

        // Intentar "comprarlo" via Webpay debe rechazarse: no requiere pago.
        Number idEstandar = obtenerIdDePlan(tokenUsuario, "ESTANDAR");
        mockMvc.perform(post("/api/ventas/iniciar")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("planId", idEstandar))))
                .andExpect(status().isConflict());
    }

    private Number obtenerIdDePlan(String tokenUsuario, String nombrePlan) throws Exception {
        String planesJson = mockMvc.perform(get("/api/planes").header("Authorization", "Bearer " + tokenUsuario))
                .andReturn().getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        var planes = objectMapper.readValue(planesJson, java.util.List.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> plan = (Map<String, Object>) planes.stream()
                .filter(p -> nombrePlan.equals(((Map<?, ?>) p).get("nombre")))
                .findFirst().orElseThrow();
        return (Number) plan.get("id");
    }

    @Test
    void flujoDePagoAprobadoActivaLaSuscripcion() throws Exception {
        String tokenUsuario = token(300L, "empresa@test.cl", "EMPRESA");
        fakePasarela.setAprobarSiguiente(true);

        Number planId = obtenerIdDePlan(tokenUsuario, "PREMIUM");

        String iniciarJson = mockMvc.perform(post("/api/ventas/iniciar")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("planId", planId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.url").exists())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        Map<String, Object> iniciar = objectMapper.readValue(iniciarJson, Map.class);
        String tokenWs = (String) iniciar.get("token");

        mockMvc.perform(post("/api/ventas/confirmar")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("tokenWs", tokenWs))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoPago", is("APROBADO")))
                .andExpect(jsonPath("$.estadoSuscripcion", is("ACTIVA")));

        mockMvc.perform(get("/api/suscripciones/mia").header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("ACTIVA")))
                .andExpect(jsonPath("$.plan", is("PREMIUM")));

        // Confirmar de nuevo (ej. el usuario recarga /pago/resultado) debe ser
        // idempotente: no vuelve a llamar a la pasarela, devuelve el mismo resultado.
        mockMvc.perform(post("/api/ventas/confirmar")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("tokenWs", tokenWs))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoPago", is("APROBADO")));
    }

    @Test
    void flujoDePagoRechazadoNoActivaLaSuscripcion() throws Exception {
        String tokenUsuario = token(301L, "cliente@test.cl", "CLIENTE");
        Number planId = obtenerIdDePlan(tokenUsuario, "PREMIUM");

        String iniciarJson = mockMvc.perform(post("/api/ventas/iniciar")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("planId", planId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        Map<String, Object> iniciar = objectMapper.readValue(iniciarJson, Map.class);
        String tokenWs = (String) iniciar.get("token");

        fakePasarela.setAprobarSiguiente(false);

        mockMvc.perform(post("/api/ventas/confirmar")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("tokenWs", tokenWs))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoPago", is("RECHAZADO")))
                .andExpect(jsonPath("$.estadoSuscripcion", is("VENCIDA")));
    }

    @Test
    void unUsuarioNoPuedeConfirmarElPagoDeOtro() throws Exception {
        String dueno = token(302L, "dueno@test.cl", "EMPRESA");
        String otro = token(303L, "otro@test.cl", "EMPRESA");
        Number planId = obtenerIdDePlan(dueno, "PREMIUM");

        String iniciarJson = mockMvc.perform(post("/api/ventas/iniciar")
                        .header("Authorization", "Bearer " + dueno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("planId", planId))))
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        Map<String, Object> iniciar = objectMapper.readValue(iniciarJson, Map.class);
        String tokenWs = (String) iniciar.get("token");

        mockMvc.perform(post("/api/ventas/confirmar")
                        .header("Authorization", "Bearer " + otro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("tokenWs", tokenWs))))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarVentasEsSoloParaAdministrador() throws Exception {
        mockMvc.perform(get("/api/ventas/todas").header("Authorization", "Bearer " + token(1L, "a@test.cl", "EMPRESA")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/ventas/todas").header("Authorization", "Bearer " + token(1L, "a@test.cl", "ADMINISTRADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void cuerpoConJsonMalformadoDevuelve400YNo500() throws Exception {
        mockMvc.perform(post("/api/ventas/iniciar")
                        .header("Authorization", "Bearer " + token(1L, "a@test.cl", "EMPRESA"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ esto no es json valido"))
                .andExpect(status().isBadRequest());
    }
}
