package com.al.lhind.hotel_reservation_api.repository;

import com.al.lhind.hotel_reservation_api.model.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
public interface GuestRepository extends JpaRepository<Guest, Long> {

    boolean existsByEmailIgnoreCase(String email);
}
