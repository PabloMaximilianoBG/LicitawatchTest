package cl.licitawatch.ventas.config;

import cl.licitawatch.ventas.dto.ErrorResponse;
import cl.licitawatch.ventas.security.JwtAuthenticationFilter;
import cl.licitawatch.ventas.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService, ObjectMapper objectMapper) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((request, response, ex) -> writeError(objectMapper, response, request.getRequestURI(), 401, "No autenticado"))
                        .accessDeniedHandler((request, response, ex) -> writeError(objectMapper, response, request.getRequestURI(), 403, "No tienes permiso para realizar esta accion")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/internal/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private Void writeError(ObjectMapper objectMapper, jakarta.servlet.http.HttpServletResponse response,
                             String path, int status, String mensaje) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        ErrorResponse body = new ErrorResponse(status, mensaje, path, LocalDateTime.now(), null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
        return null;
    }
}
