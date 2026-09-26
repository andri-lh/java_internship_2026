package org.test.cleancode.service;

import org.junit.jupiter.api.Test;
import org.test.cleancode.dto.RegisterUserRequest;
import org.test.cleancode.dto.UserResponse;
import org.test.cleancode.exception.EmailAlreadyExistsException;
import org.test.cleancode.notification.EmailNotificationStrategy;
import org.test.cleancode.notification.NotificationStrategyFactory;
import org.test.cleancode.notification.NotificationType;
import org.test.cleancode.notification.SmsNotificationStrategy;
import org.test.cleancode.repository.InMemoryUserRepository;
import org.test.cleancode.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserRegistrationServiceTest {

    @Test
    void shouldRegisterUserSuccessfully() {
        UserRegistrationService userRegistrationService = createService();
        RegisterUserRequest request =
                new RegisterUserRequest("Alice", "alice@example.com", NotificationType.EMAIL);

        UserResponse userResponse = userRegistrationService.registerUser(request);

        assertNotNull(userResponse.getId());
        assertEquals("Alice", userResponse.getName());
        assertEquals("alice@example.com", userResponse.getEmail());
    }

    @Test
    void shouldRejectDuplicateEmailRegistration() {
        UserRegistrationService userRegistrationService = createService();
        RegisterUserRequest firstRequest =
                new RegisterUserRequest("Alice", "alice@example.com", NotificationType.EMAIL);
        RegisterUserRequest secondRequest =
                new RegisterUserRequest("Bob", "alice@example.com", NotificationType.SMS);

        userRegistrationService.registerUser(firstRequest);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userRegistrationService.registerUser(secondRequest)
        );
    }

    private UserRegistrationService createService() {
        UserRepository userRepository = new InMemoryUserRepository();
        NotificationStrategyFactory notificationStrategyFactory = new NotificationStrategyFactory(
                new EmailNotificationStrategy(),
                new SmsNotificationStrategy()
        );
        return new UserRegistrationService(userRepository, notificationStrategyFactory);
    }
}
