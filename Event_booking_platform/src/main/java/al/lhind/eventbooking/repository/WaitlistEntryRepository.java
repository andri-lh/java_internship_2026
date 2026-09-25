package al.lhind.eventbooking.repository;

import al.lhind.eventbooking.entity.WaitlistEntry;
import al.lhind.eventbooking.entity.WaitlistStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, Long> {

    Optional<WaitlistEntry> findByUserIdAndEventId(Long userId, Long eventId);

    List<WaitlistEntry> findByEventIdAndStatusOrderByJoinedAtAsc(
            Long eventId,
            WaitlistStatus status
    );
}
