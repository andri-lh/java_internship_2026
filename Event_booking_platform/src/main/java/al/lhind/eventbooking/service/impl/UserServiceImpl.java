package al.lhind.eventbooking.service.impl;

import al.lhind.eventbooking.dto.request.AdminUserCreateRequest;
import al.lhind.eventbooking.dto.request.AdminUserUpdateRequest;
import al.lhind.eventbooking.dto.response.UserResponse;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long userId) {
        return toResponse(requireUser(userId));
    }

    @Override
    @Transactional
    public UserResponse create(AdminUserCreateRequest request) {
        String username = request.username().trim();
        String email = request.email().trim();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw conflict("Username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw conflict("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setActive(true);

        try {
            return toResponse(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username or email already exists",
                    exception);
        }
    }

    @Override
    @Transactional
    public UserResponse update(
            String adminUsername,
            Long userId,
            AdminUserUpdateRequest request) {

        User admin = requireActiveAdmin(adminUsername);
        User user = requireUser(userId);

        if (admin.getId().equals(userId) && request.role() != Role.ADMIN) {
            throw conflict("You cannot remove your own admin role");
        }

        String username = request.username().trim();
        String email = request.email().trim();

        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(
                username, userId)) {
            throw conflict("Username already exists");
        }
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(
                email, userId)) {
            throw conflict("Email already exists");
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setRole(request.role());

        try {
            return toResponse(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username or email already exists",
                    exception);
        }
    }

    @Override
    @Transactional
    public UserResponse setActive(
            String adminUsername, Long userId, boolean active) {

        User admin = requireActiveAdmin(adminUsername);
        User user = requireUser(userId);

        if (admin.getId().equals(userId) && !active) {
            throw conflict("You cannot deactivate your own account");
        }

        user.setActive(active);
        return toResponse(userRepository.save(user));
    }

    private User requireActiveAdmin(String username) {
        User user = userRepository.findByUsername(username)
                .filter(User::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Active admin not found"));

        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only admins can manage users");
        }

        return user;
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isActive());
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}