package com.al.lhind.hotel_reservation_api.controller;

import com.al.lhind.hotel_reservation_api.dto.ReservationDTO;
import com.al.lhind.hotel_reservation_api.dto.ReservationRequestDTO;
import com.al.lhind.hotel_reservation_api.dto.ReservationStatusUpdateDTO;
import com.al.lhind.hotel_reservation_api.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) { this.reservationService = reservationService; }

    @PostMapping
    public ResponseEntity<ReservationDTO> create(@Valid @RequestBody ReservationRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ReservationDTO>> getAll() { return ResponseEntity.ok(reservationService.getAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDTO> get(@PathVariable Long id) { return ResponseEntity.ok(reservationService.getById(id)); }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReservationDTO> updateStatus(@PathVariable Long id,
                                                         @Valid @RequestBody ReservationStatusUpdateDTO dto) {
        return ResponseEntity.ok(reservationService.updateStatus(id, dto.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        reservationService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/guest/{guestId}")
    public ResponseEntity<List<ReservationDTO>> getByGuest(@PathVariable Long guestId) {
        return ResponseEntity.ok(reservationService.getByGuest(guestId));
    }
}
