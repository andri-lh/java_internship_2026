package al.lhind.eventbooking.repository;

import al.lhind.eventbooking.entity.Event;
import al.lhind.eventbooking.entity.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizerId(Long organizerId);

    @Query(value = "select * from events where id = :eventId for update", nativeQuery = true)
    Optional<Event> findByIdForUpdate(@Param("eventId") Long eventId);

    @Query(
            value = """
        select distinct e
        from Event e
        join e.venue v
        left join e.categories c
        where e.status = :status
          and (:city is null or lower(v.city) = lower(:city))
          and (:startsAfter is null or e.startDateTime >= :startsAfter)
          and (:startsBefore is null or e.startDateTime <= :startsBefore)
          and (:minimumPrice is null or e.price >= :minimumPrice)
          and (:maximumPrice is null or e.price <= :maximumPrice)
          and (:categoryId is null or c.id = :categoryId)
        """,
            countQuery = """
        select count(distinct e.id)
        from Event e
        join e.venue v
        left join e.categories c
        where e.status = :status
          and (:city is null or lower(v.city) = lower(:city))
          and (:startsAfter is null or e.startDateTime >= :startsAfter)
          and (:startsBefore is null or e.startDateTime <= :startsBefore)
          and (:minimumPrice is null or e.price >= :minimumPrice)
          and (:maximumPrice is null or e.price <= :maximumPrice)
          and (:categoryId is null or c.id = :categoryId)
        """
    )
    Page<Event> searchEvents(
            @Param("status") EventStatus status,
            @Param("city") String city,
            @Param("startsAfter") LocalDateTime startsAfter,
            @Param("startsBefore") LocalDateTime startsBefore,
            @Param("minimumPrice") BigDecimal minimumPrice,
            @Param("maximumPrice") BigDecimal maximumPrice,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    Optional<Event> findByIdAndStatus(Long id, EventStatus status);

    boolean existsByVenueId(Long venueId);

    boolean existsByVenueIdAndTotalSeatsGreaterThan(
            Long venueId, Integer capacity);

    boolean existsByCategories_Id(Long categoryId);

}
