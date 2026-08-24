package com.al.lhind.hotel_reservation_api.controller;

import com.al.lhind.hotel_reservation_api.dto.RoomReservationReportDTO;
import com.al.lhind.hotel_reservation_api.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReservationService reservationService;

    public ReportController(ReservationService reservationService) { this.reservationService = reservationService; }

    @GetMapping("/most-reserved-rooms")
    public ResponseEntity<List<RoomReservationReportDTO>> mostReservedRooms() {
        return ResponseEntity.ok(reservationService.mostReservedRooms());
    }
}
