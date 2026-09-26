package al.lhind.eventbooking.service.impl;

import al.lhind.eventbooking.exception.AuthenticationFailureException;
import al.lhind.eventbooking.exception.BusinessConflictException;
import al.lhind.eventbooking.exception.ForbiddenOperationException;
import al.lhind.eventbooking.exception.ResourceNotFoundException;
import al.lhind.eventbooking.dto.request.AdminUserCreateRequest;
import al.lhind.eventbooking.dto.request.AdminUserUpdateRequest;
import al.lhind.eventbooking.dto.response.UserResponse;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

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
            User savedUser = userRepository.saveAndFlush(user);
            log.info("User account created by admin: userId={}, role={}", savedUser.getId(), savedUser.getRole());
            return toResponse(savedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessConflictException("Username or email already exists",
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
            User savedUser = userRepository.saveAndFlush(user);
            log.info("User account updated: userId={}, adminId={}, role={}",
                    savedUser.getId(), admin.getId(), savedUser.getRole());
            return toResponse(savedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessConflictException("Username or email already exists",
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
        User savedUser = userRepository.save(user);
        log.info("User active status changed: userId={}, adminId={}, active={}",
                savedUser.getId(), admin.getId(), savedUser.isActive());
        return toResponse(savedUser);
    }

    private User requireActiveAdmin(String username) {
        User user = userRepository.findByUsername(username)
                .filter(User::isActive)
                .orElseThrow(() -> new AuthenticationFailureException("Active admin not found"));

        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenOperationException("Only admins can manage users");
        }

        return user;
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isActive());
    }

    private BusinessConflictException conflict(String message) {
        return new BusinessConflictException(message);
    }
}