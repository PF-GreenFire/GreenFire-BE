package sisosolsol.greenfire.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import sisosolsol.greenfire.common.security.model.CustomUserDetails;
import sisosolsol.greenfire.common.security.model.SupabaseUserDTO;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {
    private final String secret;

    public JwtUtil(@Value("${supabase.jwt.secret}") String secret) {
        this.secret = secret;
    }

    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new BadCredentialsException("Invalid JWT token");
        }
    }

    public CustomUserDetails convertToUserDetails(Claims claims) {
        String customRole = extractCustomRole(claims);

        SupabaseUserDTO userDTO = SupabaseUserDTO.builder()
                .id(UUID.fromString(claims.getSubject()))
                .email(claims.get("email", String.class))
                .role(customRole)
                .build();

        return new CustomUserDetails(userDTO);
    }

    private String extractCustomRole(Claims claims) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> userMetadata = claims.get("user_metadata", Map.class);

            if (userMetadata == null || !userMetadata.containsKey("role")) {
                log.debug("No custom role found in raw_user_meta_data, using default role: USER");
                return "USER";
            }

            String role = userMetadata.get("role").toString().toUpperCase();
            log.debug("Extracted custom role: {}", role);
            return role;

        } catch (Exception e) {
            log.warn("Error while extracting custom role, using default role: USER", e);
            return "USER";
        }
    }
}