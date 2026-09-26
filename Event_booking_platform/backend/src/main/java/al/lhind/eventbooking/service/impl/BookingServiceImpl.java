package al.lhind.eventbooking.service.impl;

import al.lhind.eventbooking.exception.AuthenticationFailureException;
import al.lhind.eventbooking.exception.BusinessConflictException;
import al.lhind.eventbooking.exception.ForbiddenOperationException;
import al.lhind.eventbooking.exception.ResourceNotFoundException;
import al.lhind.eventbooking.dto.request.BookingRequest;
import al.lhind.eventbooking.dto.response.AdminBookingResponse;
import al.lhind.eventbooking.dto.response.BookingResponse;
import al.lhind.eventbooking.entity.*;
import al.lhind.eventbooking.repository.BookingRepository;
import al.lhind.eventbooking.repository.EventRepository;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.repository.WaitlistEntryRepository;
import al.lhind.eventbooking.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            EventRepository eventRepository,
            UserRepository userRepository,
            WaitlistEntryRepository waitlistEntryRepository) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.waitlistEntryRepository = waitlistEntryRepository;
    }

    @Override
    @Transactional
    public BookingResponse createBooking(
            String username,
            Long eventId,
            BookingRequest request) {

        User user = userRepository.findByUsername(username)
                .filter(User::isActive)
                .orElseThrow(() -> new AuthenticationFailureException("Active user not found"
                ));

        if (user.getRole() != Role.ATTENDEE) {
            throw new ForbiddenOperationException("Only attendees can book events"
            );
        }

        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"
                ));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessConflictException("Only published events can be booked"
            );
        }

        int seatsRequested = request.seatsBooked();
        log.debug("Booking seat check: eventId={}, requestedSeats={}, availableSeats={}",
                eventId, seatsRequested, event.getAvailableSeats());

        if (seatsRequested > event.getAvailableSeats()) {
            throw new BusinessConflictException("Not enough seats are available"
            );
        }

        event.setAvailableSeats(event.getAvailableSeats() - seatsRequested);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(event);
        booking.setSeatsBooked(seatsRequested);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking confirmed: bookingId={}, eventId={}, userId={}, seats={}, availableSeats={}",
                savedBooking.getId(), event.getId(), user.getId(), seatsRequested, event.getAvailableSeats());

        return new BookingResponse(
                savedBooking.getId(),
                savedBooking.getSeatsBooked(),
                savedBooking.getStatus().name(),
                savedBooking.getBookingDate(),
                event.getId(),
                event.getTitle()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(String username, BookingStatus status) {

        User user = userRepository.findByUsername(username)
                .filter(User::isActive)
                .orElseThrow(() -> new AuthenticationFailureException("Active user not found"
                ));

        if (user.getRole() != Role.ATTENDEE) {
            throw new ForbiddenOperationException("Only attendees can view their bookings"
            );
        }

        var bookings = (status == null)
                ? bookingRepository.findByUserId(user.getId())
                : bookingRepository.findByUserIdAndStatus(user.getId(), status);

        return bookings.stream()
                .map(booking -> new BookingResponse(
                        booking.getId(),
                        booking.getSeatsBooked(),
                        booking.getStatus().name(),
                        booking.getBookingDate(),
                        booking.getEvent().getId(),
                        booking.getEvent().getTitle()
                ))
                .toList();
    }

    @Override
    @Transactional
    public BookingResponse cancelMyBooking(String username, Long bookingId) {
        User user = userRepository.findByUsername(username)
                .filter(User::isActive)
                .orElseThrow(() -> new AuthenticationFailureException("Active user not found"
                ));

        if (user.getRole() != Role.ATTENDEE) {
            throw new ForbiddenOperationException("Only attendees can cancel their bookings"
            );
        }

        Booking booking = bookingRepository
                .findByIdAndUserIdForUpdate(bookingId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"
                ));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessConflictException("Only confirmed bookings can be cancelled"
            );
        }

        Event event = eventRepository.findByIdForUpdate(booking.getEvent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"
                ));

        log.debug("Attendee cancellation policy check: bookingId={}, eventId={}, eventStart={}",
                bookingId, event.getId(), event.getStartDateTime());
        if (event.getStartDateTime().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new BusinessConflictException("Bookings can only be cancelled at least 24 hours before the event"
            );
        }

        Booking savedBooking = cancelConfirmedBooking(booking, event);
        log.info("Booking cancelled by attendee: bookingId={}, eventId={}, userId={}, availableSeats={}",
                savedBooking.getId(), event.getId(), user.getId(), event.getAvailableSeats());

        return new BookingResponse(
                savedBooking.getId(),
                savedBooking.getSeatsBooked(),
                savedBooking.getStatus().name(),
                savedBooking.getBookingDate(),
                event.getId(),
                event.getTitle()
        );
    }

    private void promoteWaitingAttendees(Event event) {
        List<WaitlistEntry> waitingEntries =
                waitlistEntryRepository
                        .findByEventIdAndStatusOrderByJoinedAtAsc(
                                event.getId(),
                                WaitlistStatus.WAITING
                        );

        int promotionCount = Math.min(
                event.getAvailableSeats(),
                waitingEntries.size()
        );
        log.debug("Waitlist promotion check: eventId={}, availableSeats={}, waitingEntries={}, promotions={}",
                event.getId(), event.getAvailableSeats(), waitingEntries.size(), promotionCount);

        for (int i = 0; i < promotionCount; i++) {
            WaitlistEntry entry = waitingEntries.get(i);

            entry.setStatus(WaitlistStatus.PROMOTED);
            waitlistEntryRepository.save(entry);

            Booking promotedBooking = new Booking();
            promotedBooking.setUser(entry.getUser());
            promotedBooking.setEvent(event);
            promotedBooking.setSeatsBooked(1);
            promotedBooking.setStatus(BookingStatus.CONFIRMED);
            promotedBooking.setBookingDate(LocalDateTime.now());

            Booking savedPromotedBooking = bookingRepository.save(promotedBooking);
            log.info("Waitlist entry promoted: entryId={}, bookingId={}, eventId={}, userId={}",
                    entry.getId(), savedPromotedBooking.getId(), event.getId(), entry.getUser().getId());

            event.setAvailableSeats(event.getAvailableSeats() - 1);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminBookingResponse> getAllBookingsForAdmin(
            String adminUsername, Pageable pageable) {

        requireAdmin(adminUsername);

        return bookingRepository.findAllForAdmin(pageable)
                .map(this::toAdminResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminBookingResponse getBookingForAdmin(
            String adminUsername, Long bookingId) {

        requireAdmin(adminUsername);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        return toAdminResponse(booking);
    }

    @Override
    @Transactional
    public AdminBookingResponse cancelBookingForAdmin(
            String adminUsername, Long bookingId) {

        User admin = requireAdmin(adminUsername);

        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessConflictException("Only confirmed bookings can be cancelled");
        }

        Event event = eventRepository
                .findByIdForUpdate(booking.getEvent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        Booking cancelled = cancelConfirmedBooking(booking, event);
        log.info("Booking cancelled by admin: bookingId={}, eventId={}, adminId={}, availableSeats={}",
                cancelled.getId(), event.getId(), admin.getId(), event.getAvailableSeats());
        return toAdminResponse(cancelled);
    }

    private User requireAdmin(String username) {
        User user = userRepository.findByUsername(username)
                .filter(User::isActive)
                .orElseThrow(() -> new AuthenticationFailureException("Active admin not found"));

        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenOperationException("Only admins can manage all bookings");
        }

        return user;
    }

    private Booking cancelConfirmedBooking(Booking booking, Event event) {
        boolean shouldPromote =
                event.getAvailableSeats() == 0
                        && event.getStatus() == EventStatus.PUBLISHED
                        && event.getStartDateTime().isAfter(LocalDateTime.now());

        event.setAvailableSeats(
                event.getAvailableSeats() + booking.getSeatsBooked());

        booking.setStatus(BookingStatus.CANCELLED);

        if (shouldPromote) {
            promoteWaitingAttendees(event);
        }

        return bookingRepository.save(booking);
    }

    private AdminBookingResponse toAdminResponse(Booking booking) {
        Event event = booking.getEvent();

        return new AdminBookingResponse(
                booking.getId(),
                booking.getSeatsBooked(),
                booking.getStatus().name(),
                booking.getBookingDate(),
                booking.getUser().getId(),
                booking.getUser().getUsername(),
                event.getId(),
                event.getTitle(),
                event.getOrganizer().getId(),
                event.getOrganizer().getUsername()
        );
    }


}