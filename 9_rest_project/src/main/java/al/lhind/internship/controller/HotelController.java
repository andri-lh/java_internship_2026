package al.lhind.internship.controller;

import al.lhind.internship.dto.HotelDto;
import al.lhind.internship.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@Tag(name = "Hotels", description = "Create, retrieve, update, and delete hotels")
public class HotelController {

    private final HotelService hotelService;

    private static final Logger logger = LogManager.getLogger(HotelController.class);

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    @Operation(summary = "List hotels")
    public ResponseEntity<List<HotelDto>> findAll() {
        return ResponseEntity.ok(hotelService.listHotels());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a hotel by ID")
    public ResponseEntity<HotelDto> findById(@PathVariable Long id) {
        logger.info("Fetching hotel with id: {}", id);
        logger.debug("Calling hotelService.getHotelById with id: {}", id);
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    @PostMapping
    @Operation(summary = "Create a hotel")
    public ResponseEntity<HotelDto> create(@Valid @RequestBody HotelDto hotelDto) {
        return ResponseEntity.status(201).body(hotelService.createHotel(hotelDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a hotel")
    public ResponseEntity<HotelDto> update(
            @PathVariable Long id,
            @Valid @RequestBody HotelDto hotelDto) {
        return ResponseEntity.ok(hotelService.updateHotel(id, hotelDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a hotel")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }
}
