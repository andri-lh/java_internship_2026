package al.lhind.internship.controller;

import al.lhind.internship.dto.RoomDto;
import al.lhind.internship.dto.RoomStatusUpdateRequest;
import al.lhind.internship.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
@Tag(name = "Rooms", description = "Manage rooms and their availability")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/api/hotels/{hotelId}/rooms")
    @Operation(summary = "Add a room to a hotel")
    public ResponseEntity<RoomDto> addRoom(
            @PathVariable Long hotelId,
            @Valid @RequestBody RoomDto roomDto) {
        log.info("Adding room: " + roomDto.toString());
        return ResponseEntity.status(201).body(roomService.addRoom(hotelId, roomDto));
    }

    @GetMapping("/api/hotels/{hotelId}/rooms")
    @Operation(summary = "List rooms for a hotel")
    public ResponseEntity<List<RoomDto>> listRooms(@PathVariable Long hotelId) {
        log.info("Listing rooms for hotel: " + hotelId);
        return ResponseEntity.ok(roomService.listRoomsByHotelId(hotelId));
    }

    @GetMapping("/api/rooms/{id}")
    @Operation(summary = "Get a room by ID")
    public ResponseEntity<RoomDto> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @PutMapping("/api/rooms/{id}")
    @Operation(summary = "Update a room")
    public ResponseEntity<RoomDto> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomDto roomDto) {
        return ResponseEntity.ok(roomService.updateRoom(id, roomDto));
    }

    @PatchMapping("/api/rooms/{id}/status")
    @Operation(summary = "Update a room's status")
    public ResponseEntity<RoomDto> updateRoomStatus(
            @PathVariable Long id,
            @Valid @RequestBody RoomStatusUpdateRequest request) {
        return ResponseEntity.ok(roomService.updateRoomStatus(id, request.status()));
    }

    @DeleteMapping("/api/rooms/{id}")
    @Operation(summary = "Delete a room")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
