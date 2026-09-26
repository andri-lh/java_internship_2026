package al.lhind.eventbooking.service.impl;

import al.lhind.eventbooking.exception.InvalidRequestException;
import al.lhind.eventbooking.exception.ResourceNotFoundException;
import al.lhind.eventbooking.dto.response.EventResponse;
import al.lhind.eventbooking.dto.response.EventSummaryResponse;
import al.lhind.eventbooking.entity.Category;
import al.lhind.eventbooking.entity.Event;
import al.lhind.eventbooking.entity.EventStatus;
import al.lhind.eventbooking.repository.EventRepository;
import al.lhind.eventbooking.repository.ReviewRepository;
import al.lhind.eventbooking.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

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
            throw new InvalidRequestException("The start of the date range must be before its end");
        }

        if (minimumPrice != null
                && maximumPrice != null
                && minimumPrice.compareTo(maximumPrice) > 0) {
            throw new InvalidRequestException("Minimum price cannot exceed maximum price");
        }

        String normalizedCity =
                city == null || city.isBlank() ? null : city.trim();

        Page<Event> events = eventRepository.searchEvents(
                EventStatus.PUBLISHED,
                normalizedCity,
                startsAfter,
                startsBefore,
                minimumPrice,
                maximumPrice,
                categoryId,
                pageable);
        log.debug("Published event search: cityFilter={}, categoryId={}, startsAfter={}, startsBefore={}, "
                        + "minimumPrice={}, maximumPrice={}, page={}, size={}, totalMatches={}",
                normalizedCity != null, categoryId, startsAfter, startsBefore,
                minimumPrice, maximumPrice, pageable.getPageNumber(), pageable.getPageSize(),
                events.getTotalElements());
        return events.map(this::toSummaryResponse);
    }


    @Override
    @Transactional(readOnly = true)
    public EventResponse getPublishedEventById(Long eventId) {
        Event event = eventRepository
                .findByIdAndStatus(eventId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Published event not found"
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
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        return toEventResponse(event);
    }


    private EventResponse toEventResponse(Event event) {
        Double averageRating = reviewRepository
                .findAverageRatingByEventId(event.getId())
                .orElse(null);
        log.debug("Event detail assembled: eventId={}, status={}, averageRatingPresent={}",
                event.getId(), event.getStatus(), averageRating != null);

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
