package cl.licitawatch.licitaciones;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * API Licitaciones no emite JWT (eso lo hace API Usuarios) - para las
 * pruebas de integracion se firman tokens de prueba con el mismo secreto
 * compartido (jwt.secret en application.yml de test), igual que haria
 * cualquier otro microservicio que confia en el JWT emitido por Usuarios.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LicitacionFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void flujoCompletoDePublicacionBusquedaYPostulacion() throws Exception {
        String tokenEmpresa = token(100L, "empresa@test.cl", "EMPRESA");
        String tokenCliente = token(200L, "cliente@test.cl", "CLIENTE");

        Map<String, Object> nuevaLicitacion = Map.of(
                "titulo", "Mantenimiento de software 2026",
                "rubro", "Tecnologia",
                "montoEstimado", 5000000,
                "region", "Metropolitana",
                "fechaCierre", LocalDate.now().plusDays(10).toString()
        );

        String creadaJson = mockMvc.perform(post("/api/licitaciones")
                        .header("Authorization", "Bearer " + tokenEmpresa)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaLicitacion)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado", is("PUBLICADA")))
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        Map<String, Object> creada = objectMapper.readValue(creadaJson, Map.class);
        Number licitacionId = (Number) creada.get("id");

        // Un cliente no puede crear licitaciones
        mockMvc.perform(post("/api/licitaciones")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaLicitacion)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/licitaciones").header("Authorization", "Bearer " + tokenCliente)
                        .param("rubro", "Tecnologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(post("/api/licitaciones/" + licitacionId + "/postulaciones")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("propuesta", "Propuesta detallada de mantenimiento"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado", is("ENVIADA")));

        // Postular dos veces a la misma licitacion debe rechazarse
        mockMvc.perform(post("/api/licitaciones/" + licitacionId + "/postulaciones")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("propuesta", "Otra propuesta"))))
                .andExpect(status().isConflict());

        String postulacionesJson = mockMvc.perform(get("/api/licitaciones/" + licitacionId + "/postulaciones")
                        .header("Authorization", "Bearer " + tokenEmpresa))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        var postulaciones = objectMapper.readValue(postulacionesJson, java.util.List.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> primeraPostulacion = (Map<String, Object>) postulaciones.get(0);
        Number postulacionId = (Number) primeraPostulacion.get("id");

        mockMvc.perform(put("/api/licitaciones/" + licitacionId + "/postulaciones/" + postulacionId + "/estado")
                        .header("Authorization", "Bearer " + tokenEmpresa)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "ACEPTADA"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("ACEPTADA")));

        mockMvc.perform(get("/api/postulaciones/mias").header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado", is("ACEPTADA")));

        mockMvc.perform(put("/api/licitaciones/" + licitacionId + "/cerrar")
                        .header("Authorization", "Bearer " + tokenEmpresa))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CERRADA")));

        // Una licitacion cerrada ya no aparece en la busqueda publica
        mockMvc.perform(get("/api/licitaciones").header("Authorization", "Bearer " + tokenCliente)
                        .param("rubro", "Tecnologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void endpointsRequierenAutenticacion() throws Exception {
        mockMvc.perform(get("/api/licitaciones")).andExpect(status().isUnauthorized());
    }

    @Test
    void cuerpoConJsonMalformadoDevuelve400YNo500() throws Exception {
        mockMvc.perform(post("/api/licitaciones")
                        .header("Authorization", "Bearer " + token(100L, "empresa@test.cl", "EMPRESA"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ esto no es json valido"))
                .andExpect(status().isBadRequest());
    }
}
