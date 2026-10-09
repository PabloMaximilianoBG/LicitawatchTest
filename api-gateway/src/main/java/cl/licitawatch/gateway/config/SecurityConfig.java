package cl.licitawatch.gateway.config;

import cl.licitawatch.gateway.security.RespuestasJson;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Security en el gateway: JWT HS512 emitido por MS.usuarios.bs, RBAC por ruta y CORS.
 * Los microservicios internos no son accesibles desde fuera: solo aceptan tráfico con la clave interna.
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String ADMIN = "ADMINISTRADOR";
    private static final String PYME = "PYME";
    private static final String LICITADOR = "LICITADOR";

    private final RespuestasJson respuestas;

    @Bean
    public SecurityWebFilterChain seguridad(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .cors(c -> {
                })
                .authorizeExchange(a -> a
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/actuator/health").permitAll()
                        // Públicas: autenticación, catálogos de los formularios, planes, retorno de Webpay y archivos de licitaciones
                        .pathMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/catalogos/**", "/api/planes/**", "/api/licitaciones/archivos/**").permitAll()
                        .pathMatchers("/api/pagos/webpay/retorno").permitAll()
                        // RBAC
                        .pathMatchers("/api/admin/**").hasRole(ADMIN)
                        .pathMatchers("/api/suscripciones/**", "/api/ventas/**").hasRole(PYME)
                        .pathMatchers("/api/chat/**", "/api/soporte/**").hasAnyRole(LICITADOR, PYME)
                        .pathMatchers(HttpMethod.POST, "/api/licitaciones/*/postulaciones").hasRole(PYME)
                        .pathMatchers("/api/postulaciones/mias", "/api/postulaciones/uso").hasRole(PYME)
                        .pathMatchers("/api/postulaciones/*/aprobar", "/api/postulaciones/*/rechazar").hasAnyRole(LICITADOR, ADMIN)
                        .pathMatchers(HttpMethod.POST, "/api/licitaciones", "/api/licitaciones/*/imagen", "/api/licitaciones/*/archivo").hasRole(LICITADOR)
                        .pathMatchers("/api/licitaciones/mias").hasRole(LICITADOR)
                        .pathMatchers("/api/**").authenticated()
                        .anyExchange().denyAll())
                .oauth2ResourceServer(o -> o
                        .jwt(j -> j.jwtAuthenticationConverter(convertidor()))
                        .authenticationEntryPoint(respuestas::noAutenticado)
                        .accessDeniedHandler(respuestas::accesoDenegado))
                .exceptionHandling(e -> e.authenticationEntryPoint(respuestas::noAutenticado).accessDeniedHandler(respuestas::accesoDenegado))
                .build();
    }

    /** Valida firma HS512, expiración e emisor del token emitido por MS.usuarios.bs. */
    @Bean
    public ReactiveJwtDecoder jwtDecoder(@Value("${licitawatch.jwt.secret}") String secret) {
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder
                .withSecretKey(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"))
                .macAlgorithm(MacAlgorithm.HS512).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer("licitawatch")));
        return decoder;
    }

    /** Claim "rol" (LICITADOR | PYME | ADMINISTRADOR) -> ROLE_<rol>. */
    private static Converter<Jwt, Mono<AbstractAuthenticationToken>> convertidor() {
        return jwt -> {
            String rol = jwt.getClaimAsString("rol");
            List<SimpleGrantedAuthority> roles = rol == null ? List.of() : List.of(new SimpleGrantedAuthority("ROLE_" + rol));
            return Mono.just(new JwtAuthenticationToken(jwt, roles, jwt.getSubject()));
        };
    }

    /** CORS: solo el frontend configurado (FRONTEND_URL; admite varios separados por coma). */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${licitawatch.cors.origenes}") String origenes) {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(Arrays.stream(origenes.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        c.setExposedHeaders(List.of("Content-Disposition", "Retry-After"));
        c.setAllowCredentials(false);
        c.setMaxAge(Duration.ofHours(1));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }
}
