package al.lhind.internship.controller;

import al.lhind.internship.dto.RoomDto;
import al.lhind.internship.dto.RoomStatusUpdateRequest;
import al.lhind.internship.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/api/hotels/{hotelId}/rooms")
    public ResponseEntity<RoomDto> addRoom(
            @PathVariable Long hotelId,
            @Valid @RequestBody RoomDto roomDto) {
        return ResponseEntity.status(201).body(roomService.addRoom(hotelId, roomDto));
    }

    @GetMapping("/api/hotels/{hotelId}/rooms")
    public ResponseEntity<List<RoomDto>> listRooms(@PathVariable Long hotelId) {
        return ResponseEntity.ok(roomService.listRoomsByHotelId(hotelId));
    }

    @GetMapping("/api/rooms/{id}")
    public ResponseEntity<RoomDto> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @PutMapping("/api/rooms/{id}")
    public ResponseEntity<RoomDto> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomDto roomDto) {
        return ResponseEntity.ok(roomService.updateRoom(id, roomDto));
    }

    @PatchMapping("/api/rooms/{id}/status")
    public ResponseEntity<RoomDto> updateRoomStatus(
            @PathVariable Long id,
            @Valid @RequestBody RoomStatusUpdateRequest request) {
        return ResponseEntity.ok(roomService.updateRoomStatus(id, request.status()));
    }

    @DeleteMapping("/api/rooms/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
