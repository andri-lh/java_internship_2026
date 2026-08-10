package al.lhind.internship.dto;

import al.lhind.internship.entity.RoomStatus;
import al.lhind.internship.entity.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RoomDto {
    private Long id;
    private Long hotelId;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotNull(message = "Room type is required")
    private RoomType roomType;

    @NotNull(message = "Room capacity is required")
    @Positive(message = "Room capacity must be positive")
    private Integer capacity;

    @NotNull(message = "Price per night is required")
    @Positive(message = "Price per night must be positive")
    private BigDecimal pricePerNight;

    @NotNull(message = "Room status is required")
    private RoomStatus status;
}
