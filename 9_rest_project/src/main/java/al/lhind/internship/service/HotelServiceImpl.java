package al.lhind.internship.service;

import al.lhind.internship.dto.HotelDto;
import al.lhind.internship.entity.Hotel;
import al.lhind.internship.mapper.HotelMapper;
import al.lhind.internship.repository.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    public HotelServiceImpl(HotelRepository hotelRepository, HotelMapper hotelMapper) {
        this.hotelRepository = hotelRepository;
        this.hotelMapper = hotelMapper;
    }

    @Override
    public List<HotelDto> listHotels() {
        return hotelRepository.findAll().stream()
                .map(hotelMapper::toDto)
                .toList();
    }

    @Override
    public HotelDto getHotelById(Long id) {
        return hotelMapper.toDto(findHotelById(id));
    }

    @Override
    @Transactional
    public HotelDto createHotel(HotelDto hotelDto) {
        Hotel hotel = hotelMapper.toEntity(hotelDto);
        hotel.setId(null);
        return hotelMapper.toDto(hotelRepository.save(hotel));
    }

    @Override
    @Transactional
    public HotelDto updateHotel(Long id, HotelDto hotelDto) {
        Hotel hotel = findHotelById(id);
        hotelMapper.updateEntityFromDto(hotelDto, hotel);
        return hotelMapper.toDto(hotelRepository.save(hotel));
    }

    @Override
    @Transactional
    public void deleteHotel(Long id) {
        hotelRepository.delete(findHotelById(id));
    }

    private Hotel findHotelById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Hotel not found with id: " + id));
    }
}
