package com.al.lhind.hotel_reservation_api.service;

import com.al.lhind.hotel_reservation_api.dto.HotelDTO;
import com.al.lhind.hotel_reservation_api.exception.ConflictException;
import com.al.lhind.hotel_reservation_api.mapper.HotelMapper;
import com.al.lhind.hotel_reservation_api.model.entity.Hotel;
import com.al.lhind.hotel_reservation_api.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class HotelServiceTest {

    private HotelRepository hotelRepository;
    private HotelMapper hotelMapper;
    private HotelService hotelService;

    @BeforeEach
    void setUp() {
        hotelRepository = mock(HotelRepository.class);
        hotelMapper = mock(HotelMapper.class);
        hotelService = new HotelService(hotelRepository, hotelMapper);
    }

    @Test
    void shouldReturnHotelWhenItExists() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Central Park Hotel");
        HotelDTO expected = hotelDTO(1L, "Central Park Hotel");

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelMapper.toDTO(hotel)).thenReturn(expected);

        HotelDTO result = hotelService.getHotelById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Central Park Hotel", result.getName());
        verify(hotelRepository).findById(1L);
        verify(hotelMapper).toDTO(hotel);
    }

    @Test
    void shouldNotSaveHotelWhenNameAlreadyExists() {
        HotelDTO request = hotelDTO(null, "Central Park Hotel");
        when(hotelRepository.existsByName("Central Park Hotel")).thenReturn(true);

        assertThrows(ConflictException.class, () -> hotelService.createHotel(request));

        verify(hotelRepository, never()).save(any(Hotel.class));
        verifyNoInteractions(hotelMapper);
    }

    private HotelDTO hotelDTO(Long id, String name) {
        HotelDTO dto = new HotelDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setCity("Paris");
        dto.setAddress("25 Central Avenue");
        dto.setStarRating(4);
        return dto;
    }
}
