package cl.licitawatch.usuarios.config;

import cl.licitawatch.usuarios.dto.ErrorResponse;
import cl.licitawatch.usuarios.security.JwtAuthenticationFilter;
import cl.licitawatch.usuarios.security.JwtService;
import cl.licitawatch.usuarios.security.LoginRateLimitFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public LoginRateLimitFilter loginRateLimitFilter(
            ObjectMapper objectMapper,
            @Value("${licitawatch.rate-limit.login.capacidad}") int capacidad,
            @Value("${licitawatch.rate-limit.login.periodo-minutos}") int periodoMinutos) {
        return new LoginRateLimitFilter(objectMapper, capacidad, periodoMinutos);
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtService jwtService,
            LoginRateLimitFilter loginRateLimitFilter,
            ObjectMapper objectMapper) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((request, response, ex) -> writeError(objectMapper, response, request.getRequestURI(), 401, "No autenticado"))
                        .accessDeniedHandler((request, response, ex) -> writeError(objectMapper, response, request.getRequestURI(), 403, "No tienes permiso para realizar esta accion")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/internal/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")
                        .anyRequest().authenticated())
                .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
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
