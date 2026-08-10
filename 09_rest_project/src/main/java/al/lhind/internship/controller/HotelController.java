package al.lhind.internship.controller;

import al.lhind.internship.dto.HotelDto;
import al.lhind.internship.service.HotelService;
import jakarta.validation.Valid;
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
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public ResponseEntity<List<HotelDto>> findAll() {
        return ResponseEntity.ok(hotelService.listHotels());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    @PostMapping
    public ResponseEntity<HotelDto> create(@Valid @RequestBody HotelDto hotelDto) {
        return ResponseEntity.status(201).body(hotelService.createHotel(hotelDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelDto> update(
            @PathVariable Long id,
            @Valid @RequestBody HotelDto hotelDto) {
        return ResponseEntity.ok(hotelService.updateHotel(id, hotelDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }
}
