package sisosolsol.greenfire.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import sisosolsol.greenfire.common.security.model.UserRole;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class UserAccount {

    @Id
    @UuidGenerator
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "delete_reason")
    private String deleteReason;

    public UserAccount(String email, String passwordHash, UserRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public void markDeleted(String reason) {
        this.deletedAt = Instant.now();
        this.deleteReason = reason;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
