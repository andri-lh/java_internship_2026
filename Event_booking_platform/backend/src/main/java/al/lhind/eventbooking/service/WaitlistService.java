package al.lhind.eventbooking.service;

import al.lhind.eventbooking.dto.response.WaitlistResponse;
import java.util.List;

public interface WaitlistService {
    WaitlistResponse joinWaitlist(String username, Long eventId);

    List<WaitlistResponse> getMyEntries(String username);

    WaitlistResponse leaveWaitlist(String username, Long entryId);
}