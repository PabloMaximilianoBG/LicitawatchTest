package cl.licitawatch.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Valida localmente la firma del JWT emitido por API Usuarios (comparten
 * JWT_SECRET vía .env) en vez de llamar al endpoint de introspeccion en cada
 * request - seccion 4 del diseno ("puede delegar la validacion de firma
 * localmente"). Los microservicios detras del Gateway hacen exactamente lo
 * mismo con su propia copia de este mismo secreto.
 */
@Component
public class JwtValidator {

    private final Key key;

    public JwtValidator(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<Claims> validar(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
