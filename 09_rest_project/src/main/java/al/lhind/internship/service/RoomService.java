package al.lhind.internship.service;

import al.lhind.internship.dto.RoomDto;
import al.lhind.internship.entity.RoomStatus;

import java.util.List;

public interface RoomService {
    RoomDto addRoom(Long hotelId, RoomDto roomDto);
    List<RoomDto> listRoomsByHotelId(Long hotelId);
    RoomDto getRoomById(Long id);
    RoomDto updateRoom(Long id, RoomDto roomDto);
    RoomDto updateRoomStatus(Long id, RoomStatus status);
    void deleteRoom(Long id);
}
