package al.lhind.eventbooking.service.impl;

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
import al.lhind.eventbooking.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public ReviewServiceImpl(
            ReviewRepository reviewRepository,
            BookingRepository bookingRepository,
            EventRepository eventRepository,
            UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ReviewResponse createReview(
            String username,
            Long eventId,
            ReviewCreateRequest request) {

        User user = userRepository.findByUsername(username)
                .filter(User::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Active user not found"
                ));

        if (user.getRole() != Role.ATTENDEE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only attendees can review events"
            );
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Event not found"
                ));

        if (event.getEndDateTime().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An event can only be reviewed after it has ended"
            );
        }

        boolean hasConfirmedBooking =
                bookingRepository.existsByUserIdAndEventIdAndStatus(
                        user.getId(),
                        eventId,
                        BookingStatus.CONFIRMED
                );

        if (!hasConfirmedBooking) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "A confirmed booking is required to review this event"
            );
        }

        if (reviewRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You have already reviewed this event"
            );
        }

        Review review = new Review();
        review.setUser(user);
        review.setEvent(event);
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        return new ReviewResponse(
                savedReview.getId(),
                event.getId(),
                user.getUsername(),
                savedReview.getRating(),
                savedReview.getComment(),
                savedReview.getCreatedAt()
        );
    }
}