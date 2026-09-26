package al.lhind.eventbooking.service.impl;

import al.lhind.eventbooking.exception.AuthenticationFailureException;
import al.lhind.eventbooking.exception.BusinessConflictException;
import al.lhind.eventbooking.exception.ForbiddenOperationException;
import al.lhind.eventbooking.exception.ResourceNotFoundException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

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
                .orElseThrow(() -> new AuthenticationFailureException("Active user not found"
                ));

        if (user.getRole() != Role.ATTENDEE) {
            throw new ForbiddenOperationException("Only attendees can review events"
            );
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"
                ));

        if (event.getEndDateTime().isAfter(LocalDateTime.now())) {
            throw new BusinessConflictException("An event can only be reviewed after it has ended"
            );
        }

        boolean hasConfirmedBooking =
                bookingRepository.existsByUserIdAndEventIdAndStatus(
                        user.getId(),
                        eventId,
                        BookingStatus.CONFIRMED
                );

        if (!hasConfirmedBooking) {
            throw new ForbiddenOperationException("A confirmed booking is required to review this event"
            );
        }

        if (reviewRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
            throw new BusinessConflictException("You have already reviewed this event"
            );
        }

        Review review = new Review();
        review.setUser(user);
        review.setEvent(event);
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        log.info("Review created: reviewId={}, eventId={}, userId={}, rating={}",
                savedReview.getId(), event.getId(), user.getId(), savedReview.getRating());

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