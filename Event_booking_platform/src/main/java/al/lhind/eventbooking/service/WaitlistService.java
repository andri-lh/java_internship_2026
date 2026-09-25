package al.lhind.eventbooking.service;

import al.lhind.eventbooking.dto.response.WaitlistResponse;

public interface WaitlistService {
    WaitlistResponse joinWaitlist(String username, Long eventId);
}