package al.lhind.internship.repository;

import al.lhind.internship.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            SELECT COUNT(reservation)
            FROM Reservation reservation
            WHERE reservation.room.id = :roomId
              AND reservation.status <> al.lhind.internship.entity.ReservationStatus.CANCELLED
              AND reservation.checkInDate < :checkOutDate
              AND reservation.checkOutDate > :checkInDate
            """)
    long countOverlappingActiveReservations(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);


    @Query(value = """
            SELECT room_id, COUNT(id) AS reservation_count
            FROM reservations
            GROUP BY room_id
            ORDER BY reservation_count DESC
            LIMIT 5
            """, nativeQuery = true)
    List<Object[]> findTopFiveRoomsByReservationCount();

}
