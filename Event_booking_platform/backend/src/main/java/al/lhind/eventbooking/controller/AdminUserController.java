package al.lhind.eventbooking.controller;

import al.lhind.eventbooking.dto.request.AdminUserCreateRequest;
import al.lhind.eventbooking.dto.request.AdminUserUpdateRequest;
import al.lhind.eventbooking.dto.request.UserActivationRequest;
import al.lhind.eventbooking.dto.response.UserResponse;
import al.lhind.eventbooking.service.UserService;
import al.lhind.eventbooking.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "Manage user accounts and activation.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "List users")
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAll(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(userService.getAll(pageable));
    }

    @Operation(summary = "Get a user")
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getById(
            @PathVariable Long userId) {
        return ResponseEntity.ok(userService.getById(userId));
    }

    @Operation(summary = "Create a user")
    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody AdminUserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.create(request));
    }

    @Operation(summary = "Update a user")
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> update(
            Authentication authentication,
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(
                authentication.getName(), userId, request));
    }

    @Operation(summary = "Activate or deactivate a user")
    @PatchMapping("/{userId}/activation")
    public ResponseEntity<UserResponse> setActive(
            Authentication authentication,
            @PathVariable Long userId,
            @Valid @RequestBody UserActivationRequest request) {
        return ResponseEntity.ok(userService.setActive(
                authentication.getName(), userId, request.active()));
    }
}