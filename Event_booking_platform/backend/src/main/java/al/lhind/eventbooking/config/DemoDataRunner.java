package al.lhind.eventbooking.config;

import al.lhind.eventbooking.dto.request.PasswordPolicy;
import al.lhind.eventbooking.entity.Booking;
import al.lhind.eventbooking.entity.BookingStatus;
import al.lhind.eventbooking.entity.Category;
import al.lhind.eventbooking.entity.Event;
import al.lhind.eventbooking.entity.EventStatus;
import al.lhind.eventbooking.entity.Review;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.entity.Venue;
import al.lhind.eventbooking.entity.WaitlistEntry;
import al.lhind.eventbooking.entity.WaitlistStatus;
import al.lhind.eventbooking.repository.BookingRepository;
import al.lhind.eventbooking.repository.CategoryRepository;
import al.lhind.eventbooking.repository.EventRepository;
import al.lhind.eventbooking.repository.ReviewRepository;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.repository.VenueRepository;
import al.lhind.eventbooking.repository.WaitlistEntryRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Optional demo content for showcases and testing. Runs only when app.demo-data.enabled=true, needs a strong
 * shared password for the demo accounts, and does nothing if the demo accounts already exist.
 */
@Component
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataRunner.class);
    static final String MARKER_USERNAME = "demo_organizer1";

    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final String password;

    public DemoDataRunner(
            UserRepository userRepository,
            VenueRepository venueRepository,
            CategoryRepository categoryRepository,
            EventRepository eventRepository,
            BookingRepository bookingRepository,
            WaitlistEntryRepository waitlistEntryRepository,
            ReviewRepository reviewRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.demo-data.password:}") String password) {
        this.userRepository = userRepository;
        this.venueRepository = venueRepository;
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed();
    }

    @Transactional
    public void seed() {
        if (!password.matches(PasswordPolicy.REGEX)) {
            log.warn("Demo data skipped: set DEMO_DATA_PASSWORD to a password that {}", PasswordPolicy.MESSAGE);
            return;
        }
        if (userRepository.existsByUsernameIgnoreCase(MARKER_USERNAME)) {
            log.info("Demo data skipped: it already exists");
            return;
        }

        User organizer1 = user(MARKER_USERNAME, Role.ORGANIZER);
        User organizer2 = user("demo_organizer2", Role.ORGANIZER);
        User alice = user("demo_alice", Role.ATTENDEE);
        User bob = user("demo_bob", Role.ATTENDEE);
        User carol = user("demo_carol", Role.ATTENDEE);

        Venue tirana = venue("Tirana Cultural Centre", "Rruga Myslym Shyri 1", "Tirana", 300);
        Venue berat = venue("Berat Open Air Arena", "Bulevardi Republika 12", "Berat", 1000);
        Venue durres = venue("Durres Seaside Hall", "Rruga Taulantia 8", "Durres", 60);
        Venue shkoder = venue("Shkoder Lakeside Park", "Rruga e Liqenit 3", "Shkoder", 150);
        venue("Vlora Conference Hall", "Rruga Ismail Qemali 20", "Vlora", 120);

        Map<String, Category> categories = categories(
                "Music", "Technology", "Business", "Food & Drink", "Arts", "Sports", "Comedy", "Family");

        LocalDateTime now = LocalDateTime.now().withNano(0);
        Event jazz = event("Tirana Jazz Night", "An evening of live jazz with local and visiting artists.",
                now.minusDays(10), 3, "25.00", 100, EventStatus.PUBLISHED, tirana, organizer1, categories, "Music", "Arts");
        Event meetup = event("Spring Tech Meetup", "Talks and demos from the local developer community.",
                now.minusDays(30), 4, "10.00", 50, EventStatus.PUBLISHED, tirana, organizer2, categories, "Technology");
        Event festival = event("Summer Music Festival",
                "Two stages, food trucks, and a sunset headliner under the open sky.",
                now.plusDays(20), 8, "60.00", 200, EventStatus.PUBLISHED, berat, organizer1, categories, "Music", "Food & Drink");
        Event pitch = event("Startup Pitch Night",
                "Ten early-stage teams pitch to a panel of investors and mentors.",
                now.plusDays(7), 3, "15.00", 40, EventStatus.PUBLISHED, tirana, organizer2, categories, "Business", "Technology");
        Event cooking = event("Cooking Workshop: Coastal Cuisine",
                "Hands-on workshop cooking three seafood dishes. Fully booked - join the waitlist.",
                now.plusDays(3), 3, "35.00", 10, EventStatus.PUBLISHED, durres, organizer1, categories, "Food & Drink");
        Event photoWalk = event("Lakeside Photography Walk", "A guided sunrise walk around the lake. Free entry, all levels welcome.",
                now.plusDays(12), 2, "0.00", 25, EventStatus.PUBLISHED, shkoder, organizer2, categories, "Arts", "Family");
        Event comedy = event("Late Night Comedy",
                "Stand-up starting in a few hours - inside the 24h cancellation window.",
                now.plusHours(10), 3, "20.00", 30, EventStatus.PUBLISHED, tirana, organizer1, categories, "Comedy");
        Event autumnMarket = event("Autumn Food Market", "Draft: street food and local producers.",
                now.plusDays(40), 6, "5.00", 150, EventStatus.DRAFT, shkoder, organizer1, categories, "Food & Drink", "Family");
        Event charityRun = event("Charity Fun Run", "Draft: 5k charity run through the city.",
                now.plusDays(50), 4, "12.00", 120, EventStatus.DRAFT, tirana, organizer2, categories, "Sports");
        Event winterGala = event("Winter Gala", "Cancelled by the organizer.",
                now.plusDays(25), 5, "80.00", 100, EventStatus.CANCELLED, tirana, organizer2, categories, "Music", "Business");

        List<Booking> bookings = List.of(
                booking(alice, jazz, 2, BookingStatus.CONFIRMED, now.minusDays(20)),
                booking(bob, jazz, 3, BookingStatus.CONFIRMED, now.minusDays(18)),
                booking(carol, meetup, 1, BookingStatus.CONFIRMED, now.minusDays(40)),
                booking(alice, festival, 4, BookingStatus.CONFIRMED, now.minusDays(2)),
                booking(bob, pitch, 2, BookingStatus.CONFIRMED, now.minusDays(1)),
                booking(carol, pitch, 1, BookingStatus.CANCELLED, now.minusDays(3)),
                booking(alice, cooking, 5, BookingStatus.CONFIRMED, now.minusDays(5)),
                booking(bob, cooking, 5, BookingStatus.CONFIRMED, now.minusDays(4)),
                booking(alice, comedy, 2, BookingStatus.CONFIRMED, now.minusDays(1)));

        eventRepository.saveAll(List.of(jazz, meetup, festival, pitch, cooking, photoWalk, comedy,
                autumnMarket, charityRun, winterGala));
        bookingRepository.saveAll(bookings);

        WaitlistEntry waiting = new WaitlistEntry();
        waiting.setUser(carol);
        waiting.setEvent(cooking);
        waiting.setStatus(WaitlistStatus.WAITING);
        waiting.setJoinedAt(now.minusDays(1));
        waitlistEntryRepository.save(waiting);

        review(alice, jazz, 5, "Wonderful atmosphere and great musicians.", now.minusDays(9));
        review(bob, jazz, 4, "Great night, the venue was a little warm.", now.minusDays(9));
        review(carol, meetup, 4, "Useful talks and friendly people.", now.minusDays(29));

        log.info("Demo data created: 5 accounts, 5 venues, 10 events");
    }

    private User user(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@demo.example.com");
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        user.setEmailVerified(true);
        return userRepository.save(user);
    }

    private Venue venue(String name, String address, String city, int capacity) {
        Venue venue = new Venue();
        venue.setName(name);
        venue.setAddress(address);
        venue.setCity(city);
        venue.setCapacity(capacity);
        return venueRepository.save(venue);
    }

    private Map<String, Category> categories(String... names) {
        for (String name : names) {
            if (!categoryRepository.existsByNameIgnoreCase(name)) {
                Category category = new Category();
                category.setName(name);
                categoryRepository.save(category);
            }
        }
        Set<String> wanted = Set.of(names);
        return categoryRepository.findAll().stream()
                .filter(category -> wanted.contains(category.getName()))
                .collect(Collectors.toMap(Category::getName, category -> category));
    }

    private Event event(String title, String description, LocalDateTime start, int hours, String price, int seats,
                        EventStatus status, Venue venue, User organizer, Map<String, Category> categories,
                        String... categoryNames) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setStartDateTime(start);
        event.setEndDateTime(start.plusHours(hours));
        event.setPrice(new BigDecimal(price));
        event.setTotalSeats(seats);
        event.setAvailableSeats(seats);
        event.setStatus(status);
        event.setVenue(venue);
        event.setOrganizer(organizer);
        event.setCategories(Set.of(categoryNames).stream().map(categories::get).collect(Collectors.toSet()));
        event.setImageUrl("https://picsum.photos/seed/" + title.toLowerCase().replaceAll("[^a-z0-9]+", "-") + "/900/500");
        return event;
    }

    private Booking booking(User user, Event event, int seats, BookingStatus status, LocalDateTime date) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(event);
        booking.setSeatsBooked(seats);
        booking.setStatus(status);
        booking.setBookingDate(date);
        if (status == BookingStatus.CONFIRMED) {
            event.setAvailableSeats(event.getAvailableSeats() - seats);
        }
        return booking;
    }

    private void review(User user, Event event, int rating, String comment, LocalDateTime createdAt) {
        Review review = new Review();
        review.setUser(user);
        review.setEvent(event);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(createdAt);
        reviewRepository.save(review);
    }
}
