package al.lhind.internship.service;

import al.lhind.internship.dto.HotelDto;

import java.util.List;

public interface HotelService {
    List<HotelDto> listHotels();
    HotelDto getHotelById(Long id);
    HotelDto createHotel(HotelDto hotel);
    HotelDto updateHotel(Long id, HotelDto hotel);
    void deleteHotel(Long id);
}
