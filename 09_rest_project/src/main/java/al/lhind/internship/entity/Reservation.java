package al.lhind.internship.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Check-in date is required")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;

    @NotNull(message = "Number of guests is required")
    @Positive(message = "Number of guests must be positive")
    private Integer numberOfGuests;
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private LocalDateTime createdAt;

    @ManyToOne
    private Guest guest;

    @ManyToOne
    private Room room;
}
