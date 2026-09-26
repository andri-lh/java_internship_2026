package al.lhind.eventbooking.controller;

import al.lhind.eventbooking.config.OpenApiConfig;
import al.lhind.eventbooking.dto.response.VenueResponse;
import al.lhind.eventbooking.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Venues", description = "View available venues as an organizer.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/v1/organizer/venues")
public class OrganizerVenueController {

    private final VenueService venueService;

    public OrganizerVenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @Operation(summary = "List venues for event creation")
    @GetMapping
    public ResponseEntity<List<VenueResponse>> getAll() {
        return ResponseEntity.ok(venueService.getAll());
    }
}
