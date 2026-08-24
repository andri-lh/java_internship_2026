package com.al.lhind.hotel_reservation_api.controller;

import com.al.lhind.hotel_reservation_api.dto.HotelDTO;
import com.al.lhind.hotel_reservation_api.service.HotelService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    private static final Logger logger = LogManager.getLogger(HotelController.class);

    @GetMapping
    public ResponseEntity<List<HotelDTO>> getAllHotels() {
        List<HotelDTO> hotels = hotelService.getAllHotels();
        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelDTO> getHotel(@PathVariable Long id) {
        logger.info("Fetching hotel with ID: {}", id);
        logger.debug("Calling hotelService.getHotelById with ID: {}", id);
        return ResponseEntity.ok(
                hotelService.getHotelById(id)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<HotelDTO>> findByCity(@RequestParam String city) {
        return ResponseEntity.ok(hotelService.findByCity(city));
    }

    @PostMapping
    public ResponseEntity<HotelDTO> createHotel(
            @RequestBody @Valid HotelDTO hotelDTO) {

        HotelDTO createdHotel = hotelService.createHotel(hotelDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdHotel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelDTO> updateHotel(@PathVariable Long id, @RequestBody @Valid HotelDTO hotelDTO) {

        return ResponseEntity.ok(
                hotelService.updateHotel(id, hotelDTO)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {

        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }
}
