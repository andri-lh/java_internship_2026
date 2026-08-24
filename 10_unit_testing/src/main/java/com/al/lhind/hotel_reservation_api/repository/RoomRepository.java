package com.al.lhind.hotel_reservation_api.repository;

import com.al.lhind.hotel_reservation_api.enums.RoomStatus;
import com.al.lhind.hotel_reservation_api.model.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotelId(Long hotelId);
    List<Room> findByHotelIdAndStatus(Long hotelId, RoomStatus status);
}
