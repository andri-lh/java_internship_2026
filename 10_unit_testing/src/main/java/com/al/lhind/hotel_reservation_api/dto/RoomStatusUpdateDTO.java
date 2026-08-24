package com.al.lhind.hotel_reservation_api.dto;

import com.al.lhind.hotel_reservation_api.enums.RoomStatus;
import jakarta.validation.constraints.NotNull;

public record RoomStatusUpdateDTO(@NotNull RoomStatus status) {
}
