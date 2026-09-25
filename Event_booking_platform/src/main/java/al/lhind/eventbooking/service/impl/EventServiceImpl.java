package al.lhind.eventbooking.service.impl;

import al.lhind.eventbooking.dto.response.EventResponse;
import al.lhind.eventbooking.dto.response.EventSummaryResponse;
import al.lhind.eventbooking.entity.Category;
import al.lhind.eventbooking.entity.Event;
import al.lhind.eventbooking.entity.EventStatus;
import al.lhind.eventbooking.repository.EventRepository;
import al.lhind.eventbooking.repository.ReviewRepository;
import al.lhind.eventbooking.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ReviewRepository reviewRepository;

    public EventServiceImpl(
            EventRepository eventRepository,
            ReviewRepository reviewRepository) {
        this.eventRepository = eventRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventSummaryResponse> searchPublishedEvents(String city, LocalDateTime startsAfter, LocalDateTime startsBefore, BigDecimal minimumPrice, BigDecimal maximumPrice, Long categoryId, Pageable pageable) {

        if (startsAfter != null && startsBefore != null && startsAfter.isAfter(startsBefore)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The start of the date range must be before its end");
        }

        if (minimumPrice != null
                && maximumPrice != null
                && minimumPrice.compareTo(maximumPrice) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum price cannot exceed maximum price");
        }

        String normalizedCity =
                city == null || city.isBlank() ? null : city.trim();

        return eventRepository.searchEvents(
                        EventStatus.PUBLISHED,
                        normalizedCity,
                        startsAfter,
                        startsBefore,
                        minimumPrice,
                        maximumPrice,
                        categoryId,
                        pageable)
                .map(this::toSummaryResponse);
    }


    @Override
    @Transactional(readOnly = true)
    public EventResponse getPublishedEventById(Long eventId) {
        Event event = eventRepository
                .findByIdAndStatus(eventId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Published event not found"
                ));

        return toEventResponse(event);
    }

    private EventSummaryResponse toSummaryResponse(Event event) {
        return new EventSummaryResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStartDateTime(),
                event.getEndDateTime(),
                event.getPrice(),
                event.getTotalSeats(),
                event.getAvailableSeats(),
                event.getStatus().name(),
                event.getVenue().getName(),
                event.getVenue().getCity(),
                event.getOrganizer().getUsername(),
                event.getCategories().stream()
                        .map(category -> category.getName())
                        .collect(Collectors.toSet())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventSummaryResponse> getAllEventsForAdmin(Pageable pageable) {
        return eventRepository.findAll(pageable)
                .map(this::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventForAdmin(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event not found"));

        return toEventResponse(event);
    }


    private EventResponse toEventResponse(Event event) {
        Double averageRating = reviewRepository
                .findAverageRatingByEventId(event.getId())
                .orElse(null);

        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStartDateTime(),
                event.getEndDateTime(),
                event.getPrice(),
                event.getTotalSeats(),
                event.getAvailableSeats(),
                event.getStatus().name(),
                event.getVenue().getName(),
                event.getVenue().getAddress(),
                event.getVenue().getCity(),
                event.getOrganizer().getUsername(),
                event.getCategories().stream()
                        .map(Category::getName)
                        .collect(Collectors.toSet()),
                averageRating
        );
    }

}
