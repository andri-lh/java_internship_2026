package al.lhind.internship.repository;

import al.lhind.internship.entity.GuestProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestProfileRepository extends JpaRepository<GuestProfile, Long> {

    Optional<GuestProfile> findByGuest_Id(Long guestId);
}
