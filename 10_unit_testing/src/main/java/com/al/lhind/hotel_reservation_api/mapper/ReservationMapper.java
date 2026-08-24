package com.al.lhind.hotel_reservation_api.mapper;

import com.al.lhind.hotel_reservation_api.dto.ReservationDTO;
import com.al.lhind.hotel_reservation_api.model.entity.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public ReservationDTO toDTO(Reservation reservation) {
        if (reservation == null) {
            return null;
        }
        return ReservationDTO.builder()
                .id(reservation.getId())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .numberOfGuests(reservation.getNumberOfGuests())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .roomId(reservation.getRoom() != null ? reservation.getRoom().getId() : null)
                .guestId(reservation.getGuest() != null ? reservation.getGuest().getId() : null)
                .build();
    }

    public Reservation toEntity(ReservationDTO dto) {
        if (dto == null) {
            return null;
        }
        Reservation reservation = new Reservation();
        reservation.setId(dto.getId());
        reservation.setCheckInDate(dto.getCheckInDate());
        reservation.setCheckOutDate(dto.getCheckOutDate());
        reservation.setNumberOfGuests(dto.getNumberOfGuests());
        reservation.setTotalPrice(dto.getTotalPrice());
        reservation.setStatus(dto.getStatus());
        return reservation;
    }
}
