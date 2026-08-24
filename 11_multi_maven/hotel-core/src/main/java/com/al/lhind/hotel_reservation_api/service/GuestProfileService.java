package com.al.lhind.hotel_reservation_api.service;

import com.al.lhind.hotel_reservation_api.dto.GuestProfileDTO;
import com.al.lhind.hotel_reservation_api.exception.ConflictException;
import com.al.lhind.hotel_reservation_api.exception.ResourceNotFoundException;
import com.al.lhind.hotel_reservation_api.mapper.GuestProfileMapper;
import com.al.lhind.hotel_reservation_api.model.entity.Guest;
import com.al.lhind.hotel_reservation_api.model.entity.GuestProfile;
import com.al.lhind.hotel_reservation_api.repository.GuestProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class GuestProfileService {
    private final GuestService guestService;
    private final GuestProfileRepository profileRepository;
    private final GuestProfileMapper profileMapper;

    public GuestProfileService(GuestService guestService, GuestProfileRepository profileRepository,
                               GuestProfileMapper profileMapper) {
        this.guestService = guestService;
        this.profileRepository = profileRepository;
        this.profileMapper = profileMapper;
    }

    public GuestProfileDTO create(Long guestId, GuestProfileDTO dto) {
        Guest guest = guestService.findGuest(guestId);
        if (profileRepository.findByGuestId(guestId).isPresent()) {
            throw new ConflictException("Guest already has a profile");
        }
        GuestProfile profile = profileMapper.toEntity(dto);
        profile.setGuest(guest);
        return profileMapper.toDTO(profileRepository.save(profile));
    }

    public GuestProfileDTO getByGuestId(Long guestId) {
        guestService.findGuest(guestId);
        return profileMapper.toDTO(profileRepository.findByGuestId(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest profile for guest", guestId)));
    }
}
