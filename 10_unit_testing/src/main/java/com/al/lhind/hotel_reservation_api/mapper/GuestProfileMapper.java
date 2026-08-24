package com.al.lhind.hotel_reservation_api.mapper;

import com.al.lhind.hotel_reservation_api.dto.GuestProfileDTO;
import com.al.lhind.hotel_reservation_api.model.entity.GuestProfile;
import org.springframework.stereotype.Component;

@Component
public class GuestProfileMapper {

    public GuestProfileDTO toDTO(GuestProfile profile) {
        if (profile == null) {
            return null;
        }
        return GuestProfileDTO.builder()
                .id(profile.getId())
                .address(profile.getAddress())
                .dateOfBirth(profile.getDateOfBirth())
                .nationality(profile.getNationality())
                .preferredLanguage(profile.getPreferredLanguage())
                .guestId(profile.getGuest() != null ? profile.getGuest().getId() : null)
                .build();
    }

    public GuestProfile toEntity(GuestProfileDTO dto) {
        if (dto == null) {
            return null;
        }
        GuestProfile profile = new GuestProfile();
        profile.setId(dto.getId());
        profile.setAddress(dto.getAddress());
        profile.setDateOfBirth(dto.getDateOfBirth());
        profile.setNationality(dto.getNationality());
        profile.setPreferredLanguage(dto.getPreferredLanguage());
        return profile;
    }
}
