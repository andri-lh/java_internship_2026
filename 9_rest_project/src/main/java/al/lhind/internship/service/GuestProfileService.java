package al.lhind.internship.service;

import al.lhind.internship.entity.GuestProfile;

import java.util.List;

public interface GuestProfileService {
    List<GuestProfile> getAllGuestProfiles();
    GuestProfile getGuestProfileById(Long id);
    GuestProfile createGuestProfile(GuestProfile guestProfile);
    GuestProfile updateGuestProfile(Long id, GuestProfile guestProfile);
    void deleteGuestProfile(Long id);
}
