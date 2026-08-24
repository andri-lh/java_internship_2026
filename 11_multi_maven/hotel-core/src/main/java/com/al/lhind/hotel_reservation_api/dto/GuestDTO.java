package com.al.lhind.hotel_reservation_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;


public record GuestDTO (
    Long id,

    @NotBlank(message = "First name is required")
     String firstName,

    @NotBlank(message = "Last name is required")
     String lastName,

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
   String email,

    @NotBlank(message = "Phone number is required")
     String phoneNumber
){}
