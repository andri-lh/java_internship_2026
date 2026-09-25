package al.lhind.eventbooking.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserActivationRequest(@NotNull Boolean active) {}