package sisosolsol.greenfire.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sisosolsol.greenfire.user.entity.UserAccount;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<UserAccount> findByEmailAndDeletedAtIsNull(String email);

    long countByDeletedAtIsNull();
    Page<UserAccount> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}
