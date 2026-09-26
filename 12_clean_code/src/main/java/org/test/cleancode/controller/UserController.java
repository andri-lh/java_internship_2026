package org.test.cleancode.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.test.cleancode.dto.RegisterUserRequest;
import org.test.cleancode.dto.UserResponse;
import org.test.cleancode.service.UserRegistrationService;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserRegistrationService userRegistrationService;

    public UserController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(@RequestBody RegisterUserRequest request) {
        return userRegistrationService.registerUser(request);
    }
}
