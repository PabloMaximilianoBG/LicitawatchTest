package cl.licitawatch.common.seguridad;

import cl.licitawatch.common.error.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;

/**
 * Solo acepta tráfico interno (api-gateway u otro microservicio) que traiga la clave X-Internal-Key,
 * y publica el usuario autenticado que el gateway informó en las cabeceras X-User-*.
 */
@RequiredArgsConstructor
public class InternalAuthFilter extends OncePerRequestFilter {
    private final String apiKey;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = request.getRequestURI();
        return p.startsWith("/actuator/health") || p.startsWith("/swagger-ui") || p.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String recibida = req.getHeader(CabecerasInternas.INTERNAL_KEY);
        if (recibida == null || !MessageDigest.isEqual(recibida.getBytes(StandardCharsets.UTF_8), apiKey.getBytes(StandardCharsets.UTF_8))) {
            res.setStatus(401);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(res.getOutputStream(), ErrorResponse.builder().timestamp(OffsetDateTime.now()).status(401)
                    .codigo("ACCESO_INTERNO_DENEGADO").mensaje("Solo se acepta tráfico a través del api-gateway").ruta(req.getRequestURI()).build());
            return;
        }
        try {
            String id = req.getHeader(CabecerasInternas.USUARIO_ID);
            if (id != null && !id.isBlank()) {
                String perfil = req.getHeader(CabecerasInternas.PERFIL_ID);
                ContextoUsuario.establecer(new UsuarioActual(Integer.valueOf(id), req.getHeader(CabecerasInternas.USUARIO_EMAIL),
                        req.getHeader(CabecerasInternas.USUARIO_ROL), perfil == null || perfil.isBlank() ? null : Integer.valueOf(perfil)));
            }
            chain.doFilter(req, res);
        } finally {
            ContextoUsuario.limpiar();
        }
    }
}
