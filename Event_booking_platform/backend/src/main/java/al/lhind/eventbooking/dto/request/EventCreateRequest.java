package al.lhind.eventbooking.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record EventCreateRequest(
        @NotBlank @Size(max = 180)
        String title,

        @NotBlank
        String description,

        @NotNull @Future
        LocalDateTime startDateTime,

        @NotNull
        LocalDateTime endDateTime,

        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2)
        BigDecimal price,

        @NotNull @Positive
        Integer totalSeats,

        @NotNull @Positive
        Long venueId,

        @NotNull
        Set<@NotNull @Positive Long> categoryIds
) {}