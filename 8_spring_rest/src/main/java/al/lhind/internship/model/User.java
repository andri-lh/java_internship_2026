package al.lhind.internship.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    @OneToMany(mappedBy = "user")
    List<Post> posts = new ArrayList<Post>();

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }


}
