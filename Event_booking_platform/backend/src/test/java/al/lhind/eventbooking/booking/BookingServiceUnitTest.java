package al.lhind.eventbooking.booking;

import al.lhind.eventbooking.dto.request.BookingRequest;
import al.lhind.eventbooking.dto.response.BookingResponse;
import al.lhind.eventbooking.entity.Booking;
import al.lhind.eventbooking.entity.BookingStatus;
import al.lhind.eventbooking.entity.Event;
import al.lhind.eventbooking.entity.EventStatus;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.entity.WaitlistEntry;
import al.lhind.eventbooking.entity.WaitlistStatus;
import al.lhind.eventbooking.exception.BusinessConflictException;
import al.lhind.eventbooking.repository.BookingRepository;
import al.lhind.eventbooking.repository.EventRepository;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.repository.WaitlistEntryRepository;
import al.lhind.eventbooking.service.impl.BookingServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceUnitTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WaitlistEntryRepository waitlistEntryRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void bookingConsumesRequestedSeatsAndCreatesConfirmedBooking() {
        User attendee = attendee(1L);
        Event event = event(2L, 3, LocalDateTime.now().plusDays(3));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(attendee));
        when(eventRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(event));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(10L);
            return booking;
        });

        BookingResponse response = bookingService.createBooking(
                "alice", 2L, new BookingRequest(2)
        );

        assertEquals(1, event.getAvailableSeats());
        assertEquals(2, response.seatsBooked());
        assertEquals("CONFIRMED", response.status());

        ArgumentCaptor<Booking> saved = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(saved.capture());
        assertSame(attendee, saved.getValue().getUser());
        assertSame(event, saved.getValue().getEvent());
        assertEquals(BookingStatus.CONFIRMED, saved.getValue().getStatus());
    }

    @Test
    void overbookingDoesNotChangeSeatsOrSaveBooking() {
        User attendee = attendee(1L);
        Event event = event(2L, 1, LocalDateTime.now().plusDays(3));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(attendee));
        when(eventRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(event));

        BusinessConflictException failure = assertThrows(
                BusinessConflictException.class,
                () -> bookingService.createBooking("alice", 2L, new BookingRequest(2))
        );

        assertEquals(1, event.getAvailableSeats());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void cancellationWithin24HoursKeepsBookingAndSeatsUnchanged() {
        User attendee = attendee(1L);
        Event event = event(2L, 0, LocalDateTime.now().plusHours(12));
        Booking booking = booking(10L, attendee, event);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(attendee));
        when(bookingRepository.findByIdAndUserIdForUpdate(10L, 1L))
                .thenReturn(Optional.of(booking));
        when(eventRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(event));

        BusinessConflictException failure = assertThrows(
                BusinessConflictException.class,
                () -> bookingService.cancelMyBooking("alice", 10L)
        );

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(0, event.getAvailableSeats());
        verify(bookingRepository, never()).save(any());
        verifyNoInteractions(waitlistEntryRepository);
    }

    @Test
    void cancellingSoldOutBookingPromotesFirstWaitingAttendee() {
        User attendee = attendee(1L);
        Event event = event(2L, 0, LocalDateTime.now().plusDays(3));
        Booking booking = booking(10L, attendee, event);
        WaitlistEntry first = waitingEntry(attendee(3L), event);
        WaitlistEntry second = waitingEntry(attendee(4L), event);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(attendee));
        when(bookingRepository.findByIdAndUserIdForUpdate(10L, 1L))
                .thenReturn(Optional.of(booking));
        when(eventRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(event));
        when(waitlistEntryRepository.findByEventIdAndStatusOrderByJoinedAtAsc(
                2L, WaitlistStatus.WAITING
        )).thenReturn(List.of(first, second));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.cancelMyBooking("alice", 10L);

        assertEquals("CANCELLED", response.status());
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(WaitlistStatus.PROMOTED, first.getStatus());
        assertEquals(WaitlistStatus.WAITING, second.getStatus());
        assertEquals(0, event.getAvailableSeats());

        ArgumentCaptor<Booking> saved = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository, times(2)).save(saved.capture());
        Booking promoted = saved.getAllValues().getFirst();
        assertSame(first.getUser(), promoted.getUser());
        assertEquals(BookingStatus.CONFIRMED, promoted.getStatus());
        assertEquals(1, promoted.getSeatsBooked());
        verify(waitlistEntryRepository).save(first);
    }

    private User attendee(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername(id.equals(1L) ? "alice" : "attendee-" + id);
        user.setRole(Role.ATTENDEE);
        user.setActive(true);
        return user;
    }

    private Event event(Long id, int availableSeats, LocalDateTime start) {
        Event event = new Event();
        event.setId(id);
        event.setTitle("Concert");
        event.setStatus(EventStatus.PUBLISHED);
        event.setStartDateTime(start);
        event.setTotalSeats(Math.max(availableSeats, 1));
        event.setAvailableSeats(availableSeats);
        return event;
    }

    private Booking booking(Long id, User user, Event event) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setUser(user);
        booking.setEvent(event);
        booking.setSeatsBooked(1);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDateTime.now());
        return booking;
    }

    private WaitlistEntry waitingEntry(User user, Event event) {
        WaitlistEntry entry = new WaitlistEntry();
        entry.setUser(user);
        entry.setEvent(event);
        entry.setStatus(WaitlistStatus.WAITING);
        return entry;
    }
}
