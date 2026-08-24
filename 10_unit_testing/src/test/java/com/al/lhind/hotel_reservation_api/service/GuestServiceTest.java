package com.al.lhind.hotel_reservation_api.service;

import com.al.lhind.hotel_reservation_api.dto.GuestDTO;
import com.al.lhind.hotel_reservation_api.exception.ConflictException;
import com.al.lhind.hotel_reservation_api.mapper.GuestMapper;
import com.al.lhind.hotel_reservation_api.model.entity.Guest;
import com.al.lhind.hotel_reservation_api.repository.GuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GuestServiceTest {

    private GuestRepository guestRepository;
    private GuestMapper guestMapper;
    private GuestService guestService;

    @BeforeEach
    void setUp() {
        guestRepository = mock(GuestRepository.class);
        guestMapper = mock(GuestMapper.class);
        guestService = new GuestService(guestRepository, guestMapper);
    }

    @Test
    void shouldCreateGuestWhenEmailIsUnused() {
        GuestDTO request = new GuestDTO(null, "Sofia", "Martin", "sofia@example.com", "+33123456789");
        Guest guestToSave = new Guest();
        Guest savedGuest = new Guest();
        savedGuest.setId(1L);
        GuestDTO expected = new GuestDTO(1L, "Sofia", "Martin", "sofia@example.com", "+33123456789");

        when(guestRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(guestMapper.toEntity(request)).thenReturn(guestToSave);
        when(guestRepository.save(guestToSave)).thenReturn(savedGuest);
        when(guestMapper.toDTO(savedGuest)).thenReturn(expected);

        GuestDTO result = guestService.createGuest(request);

        assertEquals(1L, result.id());
        assertEquals("sofia@example.com", result.email());
        verify(guestRepository).save(guestToSave);
    }

    @Test
    void shouldNotSaveGuestWhenEmailAlreadyExists() {
        GuestDTO request = new GuestDTO(null, "Sofia", "Martin", "sofia@example.com", "+33123456789");
        when(guestRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

        assertThrows(ConflictException.class, () -> guestService.createGuest(request));

        verify(guestRepository, never()).save(any(Guest.class));
        verifyNoInteractions(guestMapper);
    }
}
