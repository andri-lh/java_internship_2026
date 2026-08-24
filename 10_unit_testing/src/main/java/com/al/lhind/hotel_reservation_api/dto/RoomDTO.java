package com.al.lhind.hotel_reservation_api.dto;

import com.al.lhind.hotel_reservation_api.enums.RoomStatus;
import com.al.lhind.hotel_reservation_api.enums.RoomType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDTO {

    private Long id;

    @Positive(message = "Room number must be greater than 0")
    private int roomNumber;

    private RoomType roomType;

    private RoomStatus status;

    @Positive(message = "Room capacity must be greater than 0")
    private int capacity;

    @Positive(message = "Price per night must be greater than 0")
    private double pricePerNight;

    private Long hotelId;
}
