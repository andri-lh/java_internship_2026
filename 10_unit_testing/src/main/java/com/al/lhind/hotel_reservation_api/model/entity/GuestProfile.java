package com.al.lhind.hotel_reservation_api.model.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@ToString(exclude = {"guest"})
@NoArgsConstructor
@AllArgsConstructor
public class GuestProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;

    private LocalDate dateOfBirth;

    private String nationality;

    private String preferredLanguage;

    @OneToOne
    @JoinColumn(name = "guest_id")
    private Guest guest;


}
