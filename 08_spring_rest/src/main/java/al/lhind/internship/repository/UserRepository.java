package al.lhind.internship.repository;

import al.lhind.internship.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.name = ?1")
    List<User> findByNameCustom(String name);



}
