package io.spring.training.boot.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String origin;
    private String destination;
    private String airline;

    @ManyToMany
    @JoinColumn(name="flight_id")
    private List<Booking> bookings;


    @Column(name = "flight_number")
    private String flightNumber;

    @Column(name = "departure_time")
    private String departureTime;

    @Column(name = "arrival_date")
    private String arrivalDate;

    private String status;

}
