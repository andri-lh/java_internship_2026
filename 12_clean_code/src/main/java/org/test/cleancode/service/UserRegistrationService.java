package org.test.cleancode.service;

import org.springframework.stereotype.Service;
import org.test.cleancode.domain.User;
import org.test.cleancode.dto.RegisterUserRequest;
import org.test.cleancode.dto.UserResponse;
import org.test.cleancode.exception.EmailAlreadyExistsException;
import org.test.cleancode.exception.InvalidRegistrationException;
import org.test.cleancode.notification.NotificationStrategy;
import org.test.cleancode.notification.NotificationStrategyFactory;
import org.test.cleancode.repository.UserRepository;

@Service
// Spring beans use singleton scope by default.
public class UserRegistrationService {
    private final UserRepository userRepository;
    private final NotificationStrategyFactory notificationStrategyFactory;

    public UserRegistrationService(
            UserRepository userRepository,
            NotificationStrategyFactory notificationStrategyFactory
    ) {
        this.userRepository = userRepository;
        this.notificationStrategyFactory = notificationStrategyFactory;
    }

    public UserResponse registerUser(RegisterUserRequest request) {
        validateRequest(request);

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        userRepository.findByEmail(normalizedEmail).ifPresent(existingUser -> {
            throw new EmailAlreadyExistsException("Email already exists: " + normalizedEmail);
        });

        User userToSave = new User(null, request.getName().trim(), normalizedEmail);
        User savedUser = userRepository.save(userToSave);

        NotificationStrategy notificationStrategy =
                notificationStrategyFactory.getStrategy(request.getNotificationType());
        notificationStrategy.sendWelcomeMessage(savedUser);

        return new UserResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail());
    }

    private void validateRequest(RegisterUserRequest request) {
        if (request == null) {
            throw new InvalidRegistrationException("Request is required");
        }
        if (isBlank(request.getName())) {
            throw new InvalidRegistrationException("Name is required");
        }
        if (isBlank(request.getEmail())) {
            throw new InvalidRegistrationException("Email is required");
        }
        if (!request.getEmail().contains("@")) {
            throw new InvalidRegistrationException("Email must contain @");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
