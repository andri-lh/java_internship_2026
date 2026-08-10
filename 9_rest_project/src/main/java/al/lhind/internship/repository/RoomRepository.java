package al.lhind.internship.repository;

import al.lhind.internship.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByHotel_Id(Long hotelId);

    Optional<Room> findByHotel_IdAndRoomNumber(Long hotelId, String roomNumber);
}
