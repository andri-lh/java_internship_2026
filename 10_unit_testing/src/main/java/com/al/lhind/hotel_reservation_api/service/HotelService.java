package com.al.lhind.hotel_reservation_api.service;

import com.al.lhind.hotel_reservation_api.dto.HotelDTO;
import com.al.lhind.hotel_reservation_api.mapper.HotelMapper;
import com.al.lhind.hotel_reservation_api.model.entity.Hotel;
import com.al.lhind.hotel_reservation_api.repository.HotelRepository;
import com.al.lhind.hotel_reservation_api.exception.ResourceNotFoundException;
import com.al.lhind.hotel_reservation_api.exception.ConflictException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    public HotelService(HotelRepository hotelRepository, HotelMapper hotelMapper) {
        this.hotelRepository = hotelRepository;
        this.hotelMapper = hotelMapper;

    }

    public List<HotelDTO> getAllHotels() {
        return hotelRepository.findAll()
                .stream()
                .map(hotelMapper::toDTO)
                .toList();
    }

    public HotelDTO getHotelById(Long id){
        Hotel hotel =  hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", id));
        return hotelMapper.toDTO(hotel);
    }

    public HotelDTO createHotel(HotelDTO hotelDTO){
        if (hotelRepository.existsByName(hotelDTO.getName())) {
            throw new ConflictException("A hotel with this name already exists");
        }

        Hotel hotel = hotelMapper.toEntity(hotelDTO);
        return hotelMapper.toDTO(hotelRepository.save(hotel));
    }

    public HotelDTO updateHotel(Long id, HotelDTO hotelDTO){
        Hotel existingHotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", id));

        existingHotel.setName(hotelDTO.getName());
        existingHotel.setCity(hotelDTO.getCity());
        existingHotel.setAddress(hotelDTO.getAddress());
        existingHotel.setStarRating(hotelDTO.getStarRating());

        hotelRepository.save(existingHotel);

        return hotelMapper.toDTO(existingHotel);
    }

    public void deleteHotel(Long id) {

        if (!hotelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hotel", id);
        }

        hotelRepository.deleteById(id);
    }

    public List<HotelDTO> findByCity(String city) {
        return hotelRepository.findByCityIgnoreCase(city).stream()
                .map(hotelMapper::toDTO)
                .toList();
    }

}

