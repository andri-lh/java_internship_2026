package al.lhind.internship.repository;

import al.lhind.internship.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    Optional<Guest> findByEmail(String email);
}
