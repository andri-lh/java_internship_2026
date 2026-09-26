package al.lhind.eventbooking.repository;

import al.lhind.eventbooking.entity.Booking;
import al.lhind.eventbooking.entity.BookingStatus;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
       select b
       from Booking b
       where b.id = :bookingId
         and b.user.id = :userId
       """)
    Optional<Booking> findByIdAndUserIdForUpdate(
            @Param("bookingId") Long bookingId,
            @Param("userId") Long userId
    );

    boolean existsByUserIdAndEventIdAndStatus(
            Long userId,
            Long eventId,
            BookingStatus status
    );

    boolean existsByEventIdAndStatus(Long eventId, BookingStatus status);

    List<Booking> findByEventIdAndStatus(Long eventId, BookingStatus status);

    @EntityGraph(attributePaths = {"user", "event"})
    @Query("""
       select b from Booking b
       where b.event.organizer.id = :organizerId
       order by b.bookingDate desc, b.id desc
       """)
    List<Booking> findForOrganizer(@Param("organizerId") Long organizerId);

    @EntityGraph(attributePaths = {"user", "event", "event.organizer"})
    @Query(
            value = "select b from Booking b",
            countQuery = "select count(b) from Booking b"
    )
    Page<Booking> findAllForAdmin(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :bookingId")
    Optional<Booking> findByIdForUpdate(
            @Param("bookingId") Long bookingId);

}
