package al.lhind.eventbooking.service;

import al.lhind.eventbooking.dto.request.ChangePasswordRequest;
import al.lhind.eventbooking.dto.request.LoginRequest;
import al.lhind.eventbooking.dto.request.RegisterRequest;
import al.lhind.eventbooking.dto.response.AuthResponse;
import al.lhind.eventbooking.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void changePassword(String username, ChangePasswordRequest request);
}