package com.al.lhind.hotel_reservation_api.repository;

import com.al.lhind.hotel_reservation_api.model.entity.GuestProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestProfileRepository extends JpaRepository<GuestProfile, Long> {
    Optional<GuestProfile> findByGuestId(Long guestId);
}
