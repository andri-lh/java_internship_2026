package al.lhind.internship.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "guest_profiles")
@Getter
@Setter
public class GuestProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;
    private LocalDate dateOfBirth;
    private String nationality;
    private String preferredLanguage;

    @OneToOne
    @JoinColumn(name = "guest_id", unique = true)
    private Guest guest;
}
