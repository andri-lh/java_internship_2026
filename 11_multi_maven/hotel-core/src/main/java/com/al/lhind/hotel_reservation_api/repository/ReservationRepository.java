package com.al.lhind.hotel_reservation_api.repository;

import com.al.lhind.hotel_reservation_api.dto.RoomReservationReportDTO;
import com.al.lhind.hotel_reservation_api.model.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByGuestId(Long guestId);

    @Query("""
            SELECT COUNT(r) FROM Reservation r
            WHERE r.room.id = :roomId AND r.status <> 'CANCELLED'
              AND r.checkInDate < :checkOutDate AND r.checkOutDate > :checkInDate
            """)
    long countOverlappingReservations(@Param("roomId") Long roomId,
                                      @Param("checkInDate") LocalDate checkInDate,
                                      @Param("checkOutDate") LocalDate checkOutDate);

    @Query(value = """
            SELECT r.room_id AS roomId, COUNT(*) AS reservationCount
            FROM reservation r WHERE r.status <> 'CANCELLED'
            GROUP BY r.room_id ORDER BY reservationCount DESC LIMIT 5
            """, nativeQuery = true)
    List<RoomReservationReportDTO> findMostReservedRooms();
}
