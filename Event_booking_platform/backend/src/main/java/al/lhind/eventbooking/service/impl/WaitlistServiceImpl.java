package al.lhind.eventbooking.service.impl;

import al.lhind.eventbooking.exception.AuthenticationFailureException;
import al.lhind.eventbooking.exception.BusinessConflictException;
import al.lhind.eventbooking.exception.ForbiddenOperationException;
import al.lhind.eventbooking.exception.ResourceNotFoundException;
import al.lhind.eventbooking.dto.response.WaitlistResponse;
import al.lhind.eventbooking.entity.Event;
import al.lhind.eventbooking.entity.EventStatus;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.entity.WaitlistEntry;
import al.lhind.eventbooking.entity.WaitlistStatus;
import al.lhind.eventbooking.repository.EventRepository;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.repository.WaitlistEntryRepository;
import al.lhind.eventbooking.service.WaitlistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class WaitlistServiceImpl implements WaitlistService {

    private static final Logger log = LoggerFactory.getLogger(WaitlistServiceImpl.class);

    private final WaitlistEntryRepository waitlistEntryRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public WaitlistServiceImpl(
            WaitlistEntryRepository waitlistEntryRepository,
            EventRepository eventRepository,
            UserRepository userRepository) {
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public WaitlistResponse joinWaitlist(String username, Long eventId) {
        User user = userRepository.findByUsername(username)
                .filter(User::isActive)
                .orElseThrow(() -> new AuthenticationFailureException("Active user not found"
                ));

        if (user.getRole() != Role.ATTENDEE) {
            throw new ForbiddenOperationException("Only attendees can join a waitlist"
            );
        }

        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"
                ));

        log.debug("Waitlist eligibility check: eventId={}, status={}, availableSeats={}",
                eventId, event.getStatus(), event.getAvailableSeats());
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessConflictException("Only published events can have a waitlist"
            );
        }

        if (event.getAvailableSeats() > 0) {
            throw new BusinessConflictException("The event still has available seats"
            );
        }

        WaitlistEntry entry = waitlistEntryRepository
                .findByUserIdAndEventId(user.getId(), event.getId())
                .orElseGet(() -> {
                    WaitlistEntry newEntry = new WaitlistEntry();
                    newEntry.setUser(user);
                    newEntry.setEvent(event);
                    return newEntry;
                });

        if (entry.getStatus() == WaitlistStatus.WAITING
                || entry.getStatus() == WaitlistStatus.PROMOTED) {
            throw new BusinessConflictException("You already have an entry for this event"
            );
        }

        entry.setJoinedAt(LocalDateTime.now());
        entry.setStatus(WaitlistStatus.WAITING);

        WaitlistEntry savedEntry = waitlistEntryRepository.save(entry);
        log.info("Waitlist joined: entryId={}, eventId={}, userId={}",
                savedEntry.getId(), event.getId(), user.getId());

        return new WaitlistResponse(
                savedEntry.getId(),
                event.getId(),
                event.getTitle(),
                savedEntry.getStatus().name(),
                savedEntry.getJoinedAt()
        );
    }
}