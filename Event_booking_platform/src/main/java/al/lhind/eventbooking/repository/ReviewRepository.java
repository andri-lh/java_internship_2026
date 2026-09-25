package al.lhind.eventbooking.repository;

import al.lhind.eventbooking.entity.Review;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    @Query("select avg(r.rating) from Review r where r.event.id = :eventId")
    Optional<Double> findAverageRatingByEventId(@Param("eventId") Long eventId);
}
