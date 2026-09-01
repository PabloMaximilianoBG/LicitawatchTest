package cl.licitawatch.notificacionesms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Abierto en desarrollo: el login del frontend contra Keycloak todavía no está integrado.
    // Cuando lo esté, reemplazar por .anyRequest().authenticated() + oauth2ResourceServer(...).
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
