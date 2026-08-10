package al.lhind.internship.service;

import al.lhind.internship.dto.RoomDto;
import al.lhind.internship.entity.Hotel;
import al.lhind.internship.entity.Room;
import al.lhind.internship.entity.RoomStatus;
import al.lhind.internship.exception.DuplicateRoomNumberException;
import al.lhind.internship.mapper.RoomMapper;
import al.lhind.internship.repository.HotelRepository;
import al.lhind.internship.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomMapper roomMapper;

    public RoomServiceImpl(
            RoomRepository roomRepository,
            HotelRepository hotelRepository,
            RoomMapper roomMapper) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.roomMapper = roomMapper;
    }

    @Override
    @Transactional
    public RoomDto addRoom(Long hotelId, RoomDto roomDto) {
        Hotel hotel = findHotelById(hotelId);
        ensureRoomNumberIsAvailable(hotelId, roomDto.getRoomNumber(), null);

        Room room = roomMapper.toEntity(roomDto);
        room.setId(null);
        room.setHotel(hotel);
        return roomMapper.toDto(roomRepository.save(room));
    }

    @Override
    public List<RoomDto> listRoomsByHotelId(Long hotelId) {
        findHotelById(hotelId);
        return roomRepository.findByHotel_Id(hotelId).stream()
                .map(roomMapper::toDto)
                .toList();
    }

    @Override
    public RoomDto getRoomById(Long id) {
        return roomMapper.toDto(findRoomById(id));
    }

    @Override
    @Transactional
    public RoomDto updateRoom(Long id, RoomDto roomDto) {
        Room room = findRoomById(id);
        ensureRoomNumberIsAvailable(room.getHotel().getId(), roomDto.getRoomNumber(), id);
        roomMapper.updateEntityFromDto(roomDto, room);
        return roomMapper.toDto(roomRepository.save(room));
    }

    @Override
    @Transactional
    public RoomDto updateRoomStatus(Long id, RoomStatus status) {
        Room room = findRoomById(id);
        room.setStatus(status);
        return roomMapper.toDto(roomRepository.save(room));
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        roomRepository.delete(findRoomById(id));
    }

    private Hotel findHotelById(Long hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NoSuchElementException("Hotel with ID " + hotelId + " was not found"));
    }

    private Room findRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Room with ID " + id + " was not found"));
    }

    private void ensureRoomNumberIsAvailable(Long hotelId, String roomNumber, Long currentRoomId) {
        roomRepository.findByHotel_IdAndRoomNumber(hotelId, roomNumber)
                .filter(room -> !room.getId().equals(currentRoomId))
                .ifPresent(room -> {
                    throw new DuplicateRoomNumberException(
                            "Room number " + roomNumber + " already exists for hotel ID " + hotelId);
                });
    }
}
