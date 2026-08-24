package com.al.lhind.hotel_reservation_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record ReservationRequestDTO(
        @NotNull Long guestId,
        @NotNull Long roomId,
        @NotNull LocalDate checkInDate,
        @NotNull LocalDate checkOutDate,
        @Positive int numberOfGuests) {
}
