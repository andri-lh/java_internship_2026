package com.al.lhind.hotel_reservation_api.dto;

import com.al.lhind.hotel_reservation_api.enums.ReservationStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDTO {

    private Long id;

    @NotNull(message = "Check-in date is required")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;

    @Positive(message = "Number of guests must be greater than 0")
    private int numberOfGuests;

    @Positive(message = "Total price must be greater than 0")
    private double totalPrice;

    private ReservationStatus status;

    private Long roomId;

    private Long guestId;
}
