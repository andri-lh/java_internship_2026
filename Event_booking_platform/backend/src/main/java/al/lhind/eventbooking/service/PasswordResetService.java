package al.lhind.eventbooking.service;

import al.lhind.eventbooking.dto.request.ForgotPasswordRequest;
import al.lhind.eventbooking.dto.request.ResetPasswordRequest;

public interface PasswordResetService {

    void requestReset(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
