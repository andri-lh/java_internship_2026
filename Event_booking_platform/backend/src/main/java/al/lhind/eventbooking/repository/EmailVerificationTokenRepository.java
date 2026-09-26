package al.lhind.eventbooking.repository;

import al.lhind.eventbooking.entity.EmailVerificationToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    @Query("select t from EmailVerificationToken t join fetch t.user where t.tokenHash = :tokenHash")
    Optional<EmailVerificationToken> findByTokenHashWithUser(@Param("tokenHash") String tokenHash);

    boolean existsByUserIdAndUsedAtIsNullAndCreatedAtAfter(Long userId, LocalDateTime createdAfter);

    @Modifying
    @Query("delete from EmailVerificationToken t where t.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("delete from EmailVerificationToken t where t.expiresAt < :cutoff or t.usedAt is not null")
    int deleteExpiredOrUsed(@Param("cutoff") LocalDateTime cutoff);
}
