package com.al.lhind.hotel_reservation_api.mapper;

import com.al.lhind.hotel_reservation_api.dto.RoomDTO;
import com.al.lhind.hotel_reservation_api.model.entity.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public RoomDTO toDTO(Room room) {
        if (room == null) {
            return null;
        }
        return RoomDTO.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .status(room.getStatus())
                .capacity(room.getCapacity())
                .pricePerNight(room.getPricePerNight())
                .hotelId(room.getHotel() != null ? room.getHotel().getId() : null)
                .build();
    }

    public Room toEntity(RoomDTO dto) {
        if (dto == null) {
            return null;
        }
        Room room = new Room();
        room.setId(dto.getId());
        room.setRoomNumber(dto.getRoomNumber());
        room.setRoomType(dto.getRoomType());
        room.setStatus(dto.getStatus());
        room.setCapacity(dto.getCapacity());
        room.setPricePerNight(dto.getPricePerNight());
        return room;
    }
}
