package com.al.lhind.hotel_reservation_api.repository;

import com.al.lhind.hotel_reservation_api.model.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    boolean existsByName(String name);

    List<Hotel> findByCityIgnoreCase(String city);
}
