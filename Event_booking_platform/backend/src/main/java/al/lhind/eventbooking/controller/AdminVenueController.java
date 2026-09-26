package al.lhind.eventbooking.controller;

import al.lhind.eventbooking.dto.request.VenueRequest;
import al.lhind.eventbooking.dto.response.VenueResponse;
import al.lhind.eventbooking.service.VenueService;
import al.lhind.eventbooking.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Venues", description = "Manage venue records.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/v1/admin/venues")
public class AdminVenueController {

    private final VenueService venueService;

    public AdminVenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @Operation(summary = "Create a venue")
    @PostMapping
    public ResponseEntity<VenueResponse> create(
            @Valid @RequestBody VenueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(venueService.create(request));
    }

    @Operation(summary = "List venues")
    @GetMapping
    public ResponseEntity<List<VenueResponse>> getAll() {
        return ResponseEntity.ok(venueService.getAll());
    }

    @Operation(summary = "Get a venue")
    @GetMapping("/{venueId}")
    public ResponseEntity<VenueResponse> getById(@PathVariable Long venueId) {
        return ResponseEntity.ok(venueService.getById(venueId));
    }

    @Operation(summary = "Update a venue")
    @PutMapping("/{venueId}")
    public ResponseEntity<VenueResponse> update(
            @PathVariable Long venueId,
            @Valid @RequestBody VenueRequest request) {
        return ResponseEntity.ok(venueService.update(venueId, request));
    }

    @Operation(summary = "Delete a venue")
    @DeleteMapping("/{venueId}")
    public ResponseEntity<Void> delete(@PathVariable Long venueId) {
        venueService.delete(venueId);
        return ResponseEntity.noContent().build();
    }
}