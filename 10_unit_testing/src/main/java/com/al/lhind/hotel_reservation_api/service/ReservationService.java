package com.al.lhind.hotel_reservation_api.service;

import com.al.lhind.hotel_reservation_api.dto.ReservationDTO;
import com.al.lhind.hotel_reservation_api.dto.ReservationRequestDTO;
import com.al.lhind.hotel_reservation_api.dto.RoomReservationReportDTO;
import com.al.lhind.hotel_reservation_api.enums.ReservationStatus;
import com.al.lhind.hotel_reservation_api.enums.RoomStatus;
import com.al.lhind.hotel_reservation_api.exception.ResourceNotFoundException;
import com.al.lhind.hotel_reservation_api.exception.RoomNotAvailableException;
import com.al.lhind.hotel_reservation_api.mapper.ReservationMapper;
import com.al.lhind.hotel_reservation_api.model.entity.Guest;
import com.al.lhind.hotel_reservation_api.model.entity.Reservation;
import com.al.lhind.hotel_reservation_api.model.entity.Room;
import com.al.lhind.hotel_reservation_api.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final GuestService guestService;
    private final RoomService roomService;
    private final ReservationMapper reservationMapper;

    public ReservationService(ReservationRepository reservationRepository, GuestService guestService,
                              RoomService roomService, ReservationMapper reservationMapper) {
        this.reservationRepository = reservationRepository;
        this.guestService = guestService;
        this.roomService = roomService;
        this.reservationMapper = reservationMapper;
    }

    public ReservationDTO create(ReservationRequestDTO request) {
        if (!request.checkOutDate().isAfter(request.checkInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
        Guest guest = guestService.findGuest(request.guestId());
        Room room = roomService.findRoom(request.roomId());
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new RoomNotAvailableException("Room is not available because it is under maintenance");
        }
        if (request.numberOfGuests() > room.getCapacity()) {
            throw new RoomNotAvailableException("Room capacity is lower than the number of guests");
        }
        if (reservationRepository.countOverlappingReservations(room.getId(), request.checkInDate(),
                request.checkOutDate()) > 0) {
            throw new RoomNotAvailableException("Room already has an overlapping active reservation");
        }
        long nights = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        Reservation reservation = new Reservation();
        reservation.setGuest(guest);
        reservation.setRoom(room);
        reservation.setCheckInDate(request.checkInDate());
        reservation.setCheckOutDate(request.checkOutDate());
        reservation.setNumberOfGuests(request.numberOfGuests());
        reservation.setTotalPrice(nights * room.getPricePerNight());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());
        return reservationMapper.toDTO(reservationRepository.save(reservation));
    }

    public List<ReservationDTO> getAll() {
        return reservationRepository.findAll().stream().map(reservationMapper::toDTO).toList();
    }

    public ReservationDTO getById(Long id) { return reservationMapper.toDTO(findReservation(id)); }

    public ReservationDTO updateStatus(Long id, ReservationStatus status) {
        Reservation reservation = findReservation(id);
        reservation.setStatus(status);
        return reservationMapper.toDTO(reservationRepository.save(reservation));
    }

    public void cancel(Long id) {
        Reservation reservation = findReservation(id);
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    public List<ReservationDTO> getByGuest(Long guestId) {
        guestService.findGuest(guestId);
        return reservationRepository.findByGuestId(guestId)
                .stream()
                .map(reservationMapper::toDTO)
                .toList();
    }

    public List<RoomReservationReportDTO> mostReservedRooms() {
        return reservationRepository.findMostReservedRooms();
    }

    private Reservation findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }
}
