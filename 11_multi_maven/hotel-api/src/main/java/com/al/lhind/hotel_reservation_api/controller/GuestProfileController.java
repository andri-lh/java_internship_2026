package com.al.lhind.hotel_reservation_api.controller;

import com.al.lhind.hotel_reservation_api.dto.GuestProfileDTO;
import com.al.lhind.hotel_reservation_api.service.GuestProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guests/{guestId}/profile")
public class GuestProfileController {
    private final GuestProfileService profileService;

    public GuestProfileController(GuestProfileService profileService) { this.profileService = profileService; }

    @PostMapping
    public ResponseEntity<GuestProfileDTO> create(@PathVariable Long guestId, @Valid @RequestBody GuestProfileDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.create(guestId, dto));
    }

    @GetMapping
    public ResponseEntity<GuestProfileDTO> get(@PathVariable Long guestId) {
        return ResponseEntity.ok(profileService.getByGuestId(guestId));
    }
}
