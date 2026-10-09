package cl.licitawatch.usuarios.bs.service.impl;

import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.usuarios.bs.dto.response.PerfilResponse;
import cl.licitawatch.usuarios.bs.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HexFormat;
import java.util.function.IntFunction;

/**
 * JWT HS512. El token de acceso usa JWT_SECRET (lo valida el api-gateway).
 * Los tokens de correo (confirmar cuenta / restablecer contraseña) usan claves DERIVADAS por propósito,
 * por lo que el gateway nunca los acepta como sesión. No requieren tablas: son autocontenidos y expiran.
 */
@Service
public class TokenServiceImpl implements TokenService {
    static final String ISSUER = "licitawatch";
    private static final String CONFIRMAR = "confirmar-cuenta";
    private static final String RESTABLECER = "restablecer-password";

    private final SecretKey claveAcceso;
    private final SecretKey claveConfirmar;
    private final SecretKey claveRestablecer;
    private final long minutosAcceso;

    public TokenServiceImpl(@Value("${licitawatch.jwt.secret}") String secret,
                            @Value("${licitawatch.jwt.expiration-minutes:60}") long minutosAcceso) {
        this.claveAcceso = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.claveConfirmar = Keys.hmacShaKeyFor(derivar(secret, CONFIRMAR));
        this.claveRestablecer = Keys.hmacShaKeyFor(derivar(secret, RESTABLECER));
        this.minutosAcceso = minutosAcceso;
    }

    @Override
    public TokenAcceso emitirAcceso(PerfilResponse p) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(Duration.ofMinutes(minutosAcceso));
        String token = Jwts.builder().issuer(ISSUER).subject(String.valueOf(p.usuarioId()))
                .claim("email", p.email()).claim("rol", p.rol()).claim("perfilId", p.perfilId())
                .claim("nombre", p.nombreVisible())
                .issuedAt(Date.from(ahora)).expiration(Date.from(expira))
                .signWith(claveAcceso, Jwts.SIG.HS512).compact();
        return new TokenAcceso(token, OffsetDateTime.ofInstant(expira, ZoneId.of("America/Santiago")));
    }

    @Override
    public String emitirConfirmacionCuenta(Integer usuarioId) {
        Instant ahora = Instant.now();
        return Jwts.builder().issuer(ISSUER).subject(String.valueOf(usuarioId)).claim("proposito", CONFIRMAR)
                .issuedAt(Date.from(ahora)).expiration(Date.from(ahora.plus(Duration.ofHours(24))))
                .signWith(claveConfirmar, Jwts.SIG.HS512).compact();
    }

    @Override
    public String emitirRestablecerPassword(Integer usuarioId, String passwordHashActual) {
        Instant ahora = Instant.now();
        return Jwts.builder().issuer(ISSUER).subject(String.valueOf(usuarioId)).claim("proposito", RESTABLECER)
                .claim("huella", huella(passwordHashActual))
                .issuedAt(Date.from(ahora)).expiration(Date.from(ahora.plus(Duration.ofMinutes(30))))
                .signWith(claveRestablecer, Jwts.SIG.HS512).compact();
    }

    @Override
    public Integer verificarConfirmacionCuenta(String token) {
        Claims c = leer(token, claveConfirmar, CONFIRMAR);
        return Integer.valueOf(c.getSubject());
    }

    @Override
    public Integer verificarRestablecerPassword(String token, IntFunction<String> hashActualDe) {
        Claims c = leer(token, claveRestablecer, RESTABLECER);
        Integer usuarioId = Integer.valueOf(c.getSubject());
        if (!huella(hashActualDe.apply(usuarioId)).equals(c.get("huella", String.class))) {
            throw new BadRequestException("TOKEN_INVALIDO", "El enlace ya fue utilizado. Solicita uno nuevo.");
        }
        return usuarioId;
    }

    private static Claims leer(String token, SecretKey clave, String proposito) {
        try {
            Claims c = Jwts.parser().verifyWith(clave).requireIssuer(ISSUER).build().parseSignedClaims(token).getPayload();
            if (!proposito.equals(c.get("proposito", String.class))) {
                throw new BadRequestException("TOKEN_INVALIDO", "El enlace no es válido");
            }
            return c;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new BadRequestException("TOKEN_EXPIRADO", "El enlace expiró. Solicita uno nuevo.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadRequestException("TOKEN_INVALIDO", "El enlace no es válido");
        }
    }

    /** Huella del hash actual: si la contraseña cambia, los enlaces anteriores dejan de servir (un solo uso). */
    static String huella(String hash) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(String.valueOf(hash).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d, 0, 12);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] derivar(String secret, String proposito) {
        try {
            return MessageDigest.getInstance("SHA-512").digest((secret + ":" + proposito).getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
