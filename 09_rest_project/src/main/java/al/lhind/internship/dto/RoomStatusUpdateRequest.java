package al.lhind.internship.dto;

import al.lhind.internship.entity.RoomStatus;
import jakarta.validation.constraints.NotNull;

public record RoomStatusUpdateRequest(
        @NotNull(message = "Room status is required") RoomStatus status) {
}
