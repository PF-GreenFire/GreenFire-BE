package sisosolsol.greenfire.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {

    private final SecretKey key;
    private final long accessExpMillis;
    private final long refreshExpMillis;

    public JwtUtil(
            @Value("${spring.jwt.secret}") String secret,
            @Value("${spring.jwt.access-exp-min}") long accessExpMin,
            @Value("${spring.jwt.refresh-exp-min:20160}") long refreshExpMin
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpMillis = accessExpMin * 60_000L;
        this.refreshExpMillis = refreshExpMin * 60_000L;
    }

    public long getAccessExpSeconds() {
        return accessExpMillis / 1000L;
    }

    public long getRefreshExpSeconds() {
        return refreshExpMillis / 1000L;
    }

    public long getRefreshExpMillis() {
        return refreshExpMillis;
    }

    public String generateAccessToken(UUID userId, String email, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessExpMillis);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(exp)
                .claim("email", email)
                .claim("role", role)
                .signWith(key)
                .compact();
    }

    /** Opaque refresh token (JWT 아님, UUID 기반) */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /** SHA-256 해시 (DB에는 해시만 저장) */
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims extractClaims(String token) {
        try {
            return parseClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("Invalid JWT token");
        }
    }

    public UUID getUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public String getEmail(Claims claims) {
        return claims.get("email", String.class);
    }

    public String getRole(Claims claims) {
        String role = claims.get("role", String.class);
        return role == null ? "USER" : role.toUpperCase();
    }
}
