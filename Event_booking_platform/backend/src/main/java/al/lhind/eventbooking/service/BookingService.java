package al.lhind.eventbooking.service;

import al.lhind.eventbooking.dto.request.BookingRequest;
import al.lhind.eventbooking.dto.response.AdminBookingResponse;
import al.lhind.eventbooking.dto.response.BookingResponse;
import al.lhind.eventbooking.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(
            String username,
            Long eventId,
            BookingRequest request
    );

    List<BookingResponse> getMyBookings(String username, BookingStatus status);

    BookingResponse cancelMyBooking(String username, Long bookingId);

    Page<AdminBookingResponse> getAllBookingsForAdmin(
            String adminUsername, Pageable pageable);

    AdminBookingResponse getBookingForAdmin(
            String adminUsername, Long bookingId);

    AdminBookingResponse cancelBookingForAdmin(
            String adminUsername, Long bookingId);

}