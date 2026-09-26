package al.lhind.eventbooking.service.impl;

import al.lhind.eventbooking.exception.AuthenticationFailureException;
import al.lhind.eventbooking.exception.ForbiddenOperationException;
import al.lhind.eventbooking.dto.response.OrganizerBookingResponse;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.repository.BookingRepository;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.service.OrganizerBookingService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizerBookingServiceImpl implements OrganizerBookingService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public OrganizerBookingServiceImpl(
            UserRepository userRepository,
            BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizerBookingResponse> getBookings(String username) {
        User organizer = userRepository.findByUsername(username)
                .filter(User::isActive)
                .orElseThrow(() -> new AuthenticationFailureException("Active user not found"));

        if (organizer.getRole() != Role.ORGANIZER) {
            throw new ForbiddenOperationException("Only organizers can view these bookings");
        }

        return bookingRepository.findForOrganizer(organizer.getId())
                .stream()
                .map(booking -> new OrganizerBookingResponse(
                        booking.getId(),
                        booking.getUser().getId(),
                        booking.getUser().getUsername(),
                        booking.getSeatsBooked(),
                        booking.getStatus().name(),
                        booking.getBookingDate(),
                        booking.getEvent().getId(),
                        booking.getEvent().getTitle()
                ))
                .toList();
    }
}