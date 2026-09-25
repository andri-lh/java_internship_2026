package al.lhind.eventbooking.service;

import al.lhind.eventbooking.dto.request.AdminUserCreateRequest;
import al.lhind.eventbooking.dto.request.AdminUserUpdateRequest;
import al.lhind.eventbooking.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<UserResponse> getAll(Pageable pageable);

    UserResponse getById(Long userId);

    UserResponse create(AdminUserCreateRequest request);

    UserResponse update(
            String adminUsername,
            Long userId,
            AdminUserUpdateRequest request);

    UserResponse setActive(
            String adminUsername,
            Long userId,
            boolean active);
}