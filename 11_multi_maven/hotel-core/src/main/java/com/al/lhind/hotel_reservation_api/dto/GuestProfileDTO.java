package com.al.lhind.hotel_reservation_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestProfileDTO {

    private Long id;

    @NotBlank(message = "Address is required")
    private String address;

    private LocalDate dateOfBirth;

    @NotBlank(message = "Nationality is required")
    private String nationality;

    @NotBlank(message = "Preferred language is required")
    private String preferredLanguage;

    private Long guestId;
}
