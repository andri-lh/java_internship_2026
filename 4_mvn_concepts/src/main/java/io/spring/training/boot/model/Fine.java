package io.spring.training.boot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fines")
@Getter
@Setter
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate issueDate;

    @ManyToOne
    @JoinColumn(name = "police_officer_id", nullable = false)
    private User policeOfficer;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FineStatus status = FineStatus.UNPAID;

    private String correctionReason;

    public Fine() {
    }

    public Fine(
            BigDecimal amount,
            String description,
            User policeOfficer,
            Vehicle vehicle
    ) {
        setAmount(amount);
        setDescription(description);
        setPoliceOfficer(policeOfficer);
        setVehicle(vehicle);

        this.issueDate = LocalDate.now();
        this.status = FineStatus.UNPAID;
    }

    public void setAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Fine amount must be greater than zero."
            );
        }

        this.amount = amount;
    }

    public void setDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Fine description cannot be empty."
            );
        }

        this.description = description;
    }

    public void setPoliceOfficer(User policeOfficer) {
        if (policeOfficer == null) {
            throw new IllegalArgumentException(
                    "Police officer cannot be null."
            );
        }

        if (policeOfficer.getRole() != Role.POLICE) {
            throw new IllegalArgumentException(
                    "Only police officers can issue fines."
            );
        }

        this.policeOfficer = policeOfficer;
    }

    public void setVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException(
                    "Vehicle cannot be null."
            );
        }

        this.vehicle = vehicle;
    }

    @Override
    public String toString() {
        return "Fine{" +
                "id=" + id +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", issueDate=" + issueDate +
                ", status=" + status +
                ", policeOfficerId=" +
                (policeOfficer != null ? policeOfficer.getId() : null) +
                ", vehicleId=" +
                (vehicle != null ? vehicle.getId() : null) +
                '}';
    }
}