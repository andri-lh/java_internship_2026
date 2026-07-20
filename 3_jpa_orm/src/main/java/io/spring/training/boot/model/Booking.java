package io.spring.training.boot.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_date")
    private LocalDate bookingDate;

    private String status;

    @ManyToMany
    @JoinColumn(name="flight_id")
    private List<Flight> flights;

}
