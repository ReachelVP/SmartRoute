package cr.ac.una.smartroute.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Emite y valida los JWT que protegen la API. Stateless: todo lo necesario viaja en el token. */
@Component
public class JwtService {

    private final SecretKey key;
    private final long expiracionMillis;

    public JwtService(
            @Value("${smartroute.jwt.secret}") String secreto,
            @Value("${smartroute.jwt.expiracion-minutos:60}") long expiracionMinutos) {
        this.key = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMillis = expiracionMinutos * 60_000;
    }

    public String generarToken(Long usuarioId, String rol) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                .claim("rol", rol)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusMillis(expiracionMillis)))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parsear(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }
}
