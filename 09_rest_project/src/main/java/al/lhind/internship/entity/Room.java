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

@Entity
@Table(name = "rooms")
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomNumber;

    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    @NotNull(message = "Room capacity is required")
    @Positive(message = "Room capacity must be positive")
    private Integer capacity;

    @NotNull(message = "Price per night is required")
    @Positive(message = "Price per night must be positive")
    private BigDecimal pricePerNight;

    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    @ManyToOne
    private Hotel hotel;
}
