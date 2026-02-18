package sisosolsol.greenfire.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token_hash", columnList = "tokenHash"),
        @Index(name = "idx_refresh_token_family", columnList = "family"),
        @Index(name = "idx_refresh_token_user_code", columnList = "user_code")
})
public class RefreshToken {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_code", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, length = 64)
    private String tokenHash;

    @Column(nullable = false, length = 36)
    private String family;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant revokedAt;

    public RefreshToken(UUID userId, String tokenHash, String family, Instant expiresAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.family = family;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public void revoke() {
        if (this.revokedAt == null) {
            this.revokedAt = Instant.now();
        }
    }
}
