package al.lhind.eventbooking.dto.response;

import al.lhind.eventbooking.entity.Role;

public record AuthResponse(String accessToken, String tokenType, Role role) {
}
