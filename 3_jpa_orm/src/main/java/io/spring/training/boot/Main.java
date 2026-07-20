package io.spring.training.boot;

import io.spring.training.boot.model.User;
import io.spring.training.boot.model.UserDetails;
import io.spring.training.boot.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("jpa-example-pu");

        EntityManager em = emf.createEntityManager();
        UserService userCrudService = new UserService(em);

        User createdUser = userCrudService.create(
                new User("john", "secret123", "USER")
        );
        System.out.println("Created: " + createdUser);

        User foundUser = userCrudService.findById(createdUser.getId());
        System.out.println("Found by id: " + foundUser);


        UserDetails userDetails = new UserDetails("John", "Doe", "john@example.com", "0123456789");

        createdUser.setUserDetails(userDetails);

        System.out.println("User details added for user: " + createdUser);

        User updatedUser = userCrudService.update(
                createdUser.getId(),
                "john.updated",
                "ADMIN"
        );
        System.out.println("Updated: " + updatedUser);

        System.out.println("All users: " + userCrudService.findAll());

        boolean deleted = userCrudService.delete(createdUser.getId());
        System.out.println("Deleted: " + deleted);

        em.close();
        emf.close();
    }
}