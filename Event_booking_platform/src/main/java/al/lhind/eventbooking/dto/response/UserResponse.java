package al.lhind.eventbooking.dto.response;

import al.lhind.eventbooking.entity.Role;

public record UserResponse(
        Long id,
        String username,
        String email,
        Role role,
        boolean active
) {
}