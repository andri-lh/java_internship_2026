package com.al.lhind.hotel_reservation_api.controller;

import com.al.lhind.hotel_reservation_api.dto.RoomDTO;
import com.al.lhind.hotel_reservation_api.dto.RoomStatusUpdateDTO;
import com.al.lhind.hotel_reservation_api.enums.RoomStatus;
import com.al.lhind.hotel_reservation_api.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) { this.roomService = roomService; }

    @PostMapping("/hotels/{hotelId}/rooms")
    public ResponseEntity<RoomDTO> create(@PathVariable Long hotelId, @Valid @RequestBody RoomDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(hotelId, dto));
    }

    @GetMapping("/hotels/{hotelId}/rooms")
    public ResponseEntity<List<RoomDTO>> getByHotel(@PathVariable Long hotelId) {
        return ResponseEntity.ok(roomService.getRoomsByHotel(hotelId));
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<RoomDTO> get(@PathVariable Long id) { return ResponseEntity.ok(roomService.getRoom(id)); }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<RoomDTO> update(@PathVariable Long id, @Valid @RequestBody RoomDTO dto) {
        return ResponseEntity.ok(roomService.updateRoom(id, dto));
    }

    @PatchMapping("/rooms/{id}/status")
    public ResponseEntity<RoomDTO> updateStatus(@PathVariable Long id, @Valid @RequestBody RoomStatusUpdateDTO dto) {
        return ResponseEntity.ok(roomService.updateStatus(id, dto.status()));
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rooms/search")
    public ResponseEntity<List<RoomDTO>> search(@RequestParam Long hotelId, @RequestParam RoomStatus status) {
        return ResponseEntity.ok(roomService.search(hotelId, status));
    }
}
