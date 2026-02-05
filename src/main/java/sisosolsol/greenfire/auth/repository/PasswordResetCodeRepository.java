package sisosolsol.greenfire.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sisosolsol.greenfire.auth.entity.PasswordResetCode;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, UUID> {

    Optional<PasswordResetCode> findByEmailAndCodeAndVerifiedFalse(String email, String code);

    Optional<PasswordResetCode> findByEmailAndCodeAndVerifiedTrue(String email, String code);

    void deleteByEmail(String email);
}
