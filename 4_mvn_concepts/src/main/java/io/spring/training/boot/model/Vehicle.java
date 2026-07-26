package io.spring.training.boot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String plateNumber;

    @Column(nullable = false, unique = true)
    private String vin;

    @ManyToOne
    @JoinColumn(name = "user_id",  nullable = false)
    private User owner;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL)
    private List<Fine> fines = new ArrayList<>();

    public Vehicle() {
    }

    public Vehicle(String plateNumber, String vin) {
        this.plateNumber = plateNumber;
        this.vin = vin;
    }

    public void addFine(Fine fine) {
        fines.add(fine);
        fine.setVehicle(this);
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", plateNumber='" + plateNumber + '\'' +
                ", vin='" + vin + '\'' +
                '}';
    }


}
