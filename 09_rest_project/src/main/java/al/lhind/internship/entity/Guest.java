package al.lhind.internship.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guests")
@Getter
@Setter
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    private String phoneNumber;

    @Valid
    @OneToOne(mappedBy = "guest", cascade = CascadeType.ALL, orphanRemoval = true)
    private GuestProfile guestProfile;

    @Valid
    @OneToMany(mappedBy = "guest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();
}
