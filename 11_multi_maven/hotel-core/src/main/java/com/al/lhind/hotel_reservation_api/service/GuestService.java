package com.al.lhind.hotel_reservation_api.service;

import com.al.lhind.hotel_reservation_api.dto.GuestDTO;
import com.al.lhind.hotel_reservation_api.mapper.GuestMapper;
import com.al.lhind.hotel_reservation_api.model.entity.Guest;
import com.al.lhind.hotel_reservation_api.repository.GuestRepository;
import com.al.lhind.hotel_reservation_api.exception.ConflictException;
import com.al.lhind.hotel_reservation_api.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GuestService {

    private final GuestRepository guestRepository;
    private final GuestMapper guestMapper;

    public GuestService(GuestRepository guestRepository, GuestMapper guestMapper){
        this.guestRepository = guestRepository;
        this.guestMapper = guestMapper;
    }

    public GuestDTO createGuest(GuestDTO guestDTO){

        if (guestRepository.existsByEmailIgnoreCase(guestDTO.email())) {
            throw new ConflictException("A guest with this email already exists");
        }
        return guestMapper.toDTO(guestRepository.save(guestMapper.toEntity(guestDTO)));
    }

    public List<GuestDTO> getAllGuests() {
        return guestRepository.findAll().stream().map(guestMapper::toDTO).toList();
    }

    public GuestDTO getGuestById(Long id) {
        return guestMapper.toDTO(findGuest(id));
    }

    public GuestDTO updateGuest(Long id, GuestDTO dto) {
        Guest guest = findGuest(id);
        if (!guest.getEmail().equalsIgnoreCase(dto.email()) && guestRepository.existsByEmailIgnoreCase(dto.email())) {
            throw new ConflictException("A guest with this email already exists");
        }
        guest.setFirstName(dto.firstName());
        guest.setLastName(dto.lastName());
        guest.setEmail(dto.email());
        guest.setPhoneNumber(dto.phoneNumber());
        return guestMapper.toDTO(guestRepository.save(guest));
    }

    public Guest findGuest(Long id) {
        return guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest", id));
    }

}
