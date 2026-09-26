package al.lhind.eventbooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyEmailRequest(
        @NotBlank
        @Size(max = 200)
        String token
) {
}
