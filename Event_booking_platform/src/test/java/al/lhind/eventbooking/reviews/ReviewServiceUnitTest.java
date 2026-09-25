package al.lhind.eventbooking.reviews;

import al.lhind.eventbooking.dto.request.ReviewCreateRequest;
import al.lhind.eventbooking.dto.response.ReviewResponse;
import al.lhind.eventbooking.entity.BookingStatus;
import al.lhind.eventbooking.entity.Event;
import al.lhind.eventbooking.entity.Review;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.repository.BookingRepository;
import al.lhind.eventbooking.repository.EventRepository;
import al.lhind.eventbooking.repository.ReviewRepository;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.service.impl.ReviewServiceImpl;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceUnitTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    void confirmedAttendeeCanReviewCompletedEvent() {
        User attendee = attendee();
        Event event = event(LocalDateTime.now().minusHours(1));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(attendee));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(event));
        when(bookingRepository.existsByUserIdAndEventIdAndStatus(
                1L, 2L, BookingStatus.CONFIRMED
        )).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(10L);
            return review;
        });

        ReviewResponse response = reviewService.createReview(
                "alice", 2L, new ReviewCreateRequest(5, "Great event")
        );

        assertEquals(10L, response.id());
        assertEquals(5, response.rating());
        ArgumentCaptor<Review> saved = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(saved.capture());
        assertSame(attendee, saved.getValue().getUser());
        assertSame(event, saved.getValue().getEvent());
    }

    @Test
    void missingConfirmedBookingPreventsReview() {
        User attendee = attendee();
        Event event = event(LocalDateTime.now().minusHours(1));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(attendee));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(event));

        ResponseStatusException failure = assertThrows(
                ResponseStatusException.class,
                () -> reviewService.createReview(
                        "alice", 2L, new ReviewCreateRequest(5, "Great event")
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, failure.getStatusCode());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void reviewMustWaitUntilEventHasEnded() {
        User attendee = attendee();
        Event event = event(LocalDateTime.now().plusHours(1));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(attendee));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(event));

        ResponseStatusException failure = assertThrows(
                ResponseStatusException.class,
                () -> reviewService.createReview(
                        "alice", 2L, new ReviewCreateRequest(5, "Too early")
                )
        );

        assertEquals(HttpStatus.CONFLICT, failure.getStatusCode());
        verifyNoInteractions(bookingRepository, reviewRepository);
    }

    @Test
    void duplicateReviewIsRejectedBeforeSaving() {
        User attendee = attendee();
        Event event = event(LocalDateTime.now().minusHours(1));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(attendee));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(event));
        when(bookingRepository.existsByUserIdAndEventIdAndStatus(
                1L, 2L, BookingStatus.CONFIRMED
        )).thenReturn(true);
        when(reviewRepository.existsByUserIdAndEventId(1L, 2L)).thenReturn(true);

        ResponseStatusException failure = assertThrows(
                ResponseStatusException.class,
                () -> reviewService.createReview(
                        "alice", 2L, new ReviewCreateRequest(5, "Again")
                )
        );

        assertEquals(HttpStatus.CONFLICT, failure.getStatusCode());
        verify(reviewRepository, never()).save(any());
    }

    private User attendee() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setRole(Role.ATTENDEE);
        user.setActive(true);
        return user;
    }

    private Event event(LocalDateTime end) {
        Event event = new Event();
        event.setId(2L);
        event.setEndDateTime(end);
        return event;
    }
}
