package al.lhind.internship.service;

import al.lhind.internship.entity.Guest;

import java.util.List;

public interface GuestService {
    List<Guest> getAllGuests();
    Guest getGuestById(Long id);
    Guest createGuest(Guest guest);
    Guest updateGuest(Long id, Guest guest);
    void deleteGuest(Long id);
}
