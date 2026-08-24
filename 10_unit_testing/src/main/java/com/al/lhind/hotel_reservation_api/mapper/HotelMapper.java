package com.al.lhind.hotel_reservation_api.mapper;

import com.al.lhind.hotel_reservation_api.dto.HotelDTO;
import com.al.lhind.hotel_reservation_api.model.entity.Hotel;
import org.springframework.stereotype.Component;

@Component
public class HotelMapper {

    public HotelDTO toDTO(Hotel hotel) {
        if (hotel == null) {
            return null;
        }
        return HotelDTO.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .city(hotel.getCity())
                .address(hotel.getAddress())
                .starRating(hotel.getStarRating())
                .build();
    }

    public Hotel toEntity(HotelDTO dto) {
        if (dto == null) {
            return null;
        }
        Hotel hotel = new Hotel();
        hotel.setId(dto.getId());
        hotel.setName(dto.getName());
        hotel.setCity(dto.getCity());
        hotel.setAddress(dto.getAddress());
        hotel.setStarRating(dto.getStarRating());
        return hotel;
    }
}
