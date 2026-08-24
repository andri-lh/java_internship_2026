package com.al.lhind.hotel_reservation_api.dto;

import com.al.lhind.hotel_reservation_api.enums.ReservationStatus;
import jakarta.validation.constraints.NotNull;

public record ReservationStatusUpdateDTO(@NotNull ReservationStatus status) {
}
