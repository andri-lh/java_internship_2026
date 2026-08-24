package com.al.lhind.hotel_reservation_api.service;

import com.al.lhind.hotel_reservation_api.dto.RoomDTO;
import com.al.lhind.hotel_reservation_api.enums.RoomStatus;
import com.al.lhind.hotel_reservation_api.exception.ResourceNotFoundException;
import com.al.lhind.hotel_reservation_api.mapper.RoomMapper;
import com.al.lhind.hotel_reservation_api.model.entity.Hotel;
import com.al.lhind.hotel_reservation_api.model.entity.Room;
import com.al.lhind.hotel_reservation_api.repository.HotelRepository;
import com.al.lhind.hotel_reservation_api.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomMapper roomMapper;

    public RoomService(RoomRepository roomRepository, HotelRepository hotelRepository, RoomMapper roomMapper) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.roomMapper = roomMapper;
    }

    public RoomDTO createRoom(Long hotelId, RoomDTO dto) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));
        Room room = roomMapper.toEntity(dto);
        room.setHotel(hotel);
        if (room.getStatus() == null) room.setStatus(RoomStatus.AVAILABLE);
        return roomMapper.toDTO(roomRepository.save(room));
    }

    public List<RoomDTO> getRoomsByHotel(Long hotelId) {
        if (!hotelRepository.existsById(hotelId)) throw new ResourceNotFoundException("Hotel", hotelId);
        return roomRepository.findByHotelId(hotelId)
                .stream()
                .map(roomMapper::toDTO)
                .toList();
    }

    public RoomDTO getRoom(Long id) { return roomMapper.toDTO(findRoom(id)); }

    public RoomDTO updateRoom(Long id, RoomDTO dto) {
        Room room = findRoom(id);
        room.setRoomNumber(dto.getRoomNumber());
        room.setRoomType(dto.getRoomType());
        room.setStatus(dto.getStatus());
        room.setCapacity(dto.getCapacity());
        room.setPricePerNight(dto.getPricePerNight());
        return roomMapper.toDTO(roomRepository.save(room));
    }

    public RoomDTO updateStatus(Long id, RoomStatus status) {
        Room room = findRoom(id);
        room.setStatus(status);
        return roomMapper.toDTO(roomRepository.save(room));
    }

    public void deleteRoom(Long id) {
        roomRepository.delete(findRoom(id));
    }

    public List<RoomDTO> search(Long hotelId, RoomStatus status) {
        if (!hotelRepository.existsById(hotelId)) throw new ResourceNotFoundException("Hotel", hotelId);
        return roomRepository.findByHotelIdAndStatus(hotelId, status)
                .stream()
                .map(roomMapper::toDTO)
                .toList();
    }

    public Room findRoom(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room", id));
    }
}
