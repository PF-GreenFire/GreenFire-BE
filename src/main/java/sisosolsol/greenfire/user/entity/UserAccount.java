package sisosolsol.greenfire.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import sisosolsol.greenfire.common.security.model.UserRole;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
@SecondaryTable(name = "auth_account",
        pkJoinColumns = @PrimaryKeyJoinColumn(name = "user_code", referencedColumnName = "user_code"))
public class UserAccount {

    @Id
    @UuidGenerator
    @Column(name = "user_code", columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "birth")
    private LocalDate birth;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "profile_key")
    private String profileKey;

    @Column(name = "cover_image_key")
    private String coverImageKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", table = "auth_account", nullable = false)
    private UserRole role = UserRole.USER;

    @Column(name = "deleted_at", table = "auth_account")
    private Instant deletedAt;

    @Column(name = "delete_reason", table = "auth_account")
    private String deleteReason;

    @Column(name = "suspended_until", table = "auth_account")
    private Instant suspendedUntil;

    @Column(name = "suspend_reason", table = "auth_account")
    private String suspendReason;

    public UserAccount(String email, String passwordHash, UserRole role, String name, String nickname,
                       LocalDate birth, String gender, String phone) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.name = name;
        this.nickname = nickname;
        this.birth = birth;
        this.gender = gender;
        this.phone = phone;
    }

    public void updateProfileKey(String profileKey) {
        this.profileKey = profileKey;
    }

    public void updatePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void updateRole(UserRole newRole) {
        this.role = newRole;
    }

    public void markDeleted(String reason) {
        this.deletedAt = Instant.now();
        this.deleteReason = reason;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public void suspend(Instant until, String reason) {
        this.suspendedUntil = until;
        this.suspendReason = reason;
    }

    public void unsuspend() {
        this.suspendedUntil = null;
        this.suspendReason = null;
    }

    public boolean isSuspended() {
        return this.suspendedUntil != null && Instant.now().isBefore(this.suspendedUntil);
    }
}
