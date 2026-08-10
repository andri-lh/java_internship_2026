package al.lhind.internship.repository;

import al.lhind.internship.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    Hotel findByCityIgnoreCase(String city);





}
