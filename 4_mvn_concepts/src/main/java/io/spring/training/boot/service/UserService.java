package io.spring.training.boot.service;

import io.spring.training.boot.model.Role;
import io.spring.training.boot.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class UserService {

    private final EntityManager entityManager;

    public UserService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public User createUser(
            String firstName,
            String lastName,
            String nationalId,
            Role role
    ) {
        validateUserData(firstName, lastName, nationalId, role);

        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            if (nationalIdExists(nationalId)) {
                throw new IllegalArgumentException(
                        "A user with this national ID already exists."
                );
            }

            User user = new User(
                    firstName,
                    lastName,
                    nationalId,
                    role
            );

            entityManager.persist(user);
            transaction.commit();

            return user;

        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public User findById(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null.");
        }

        User user = entityManager.find(User.class, userId);

        if (user == null) {
            throw new IllegalArgumentException(
                    "User with ID " + userId + " was not found."
            );
        }

        return user;
    }

    public List<User> findAll() {
        return entityManager
                .createQuery(
                        "SELECT u FROM User u ORDER BY u.id",
                        User.class
                )
                .getResultList();
    }

    public User updateUser(
            Integer userId,
            String firstName,
            String lastName,
            String nationalId,
            Role role
    ) {
        validateUserData(firstName, lastName, nationalId, role);

        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            User user = entityManager.find(User.class, userId);

            if (user == null) {
                throw new IllegalArgumentException(
                        "User with ID " + userId + " was not found."
                );
            }

            if (!nationalId.equals(user.getNationalId())
                    && nationalIdExists(nationalId)) {
                throw new IllegalArgumentException(
                        "A user with this national ID already exists."
                );
            }

            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setNationalId(nationalId);
            user.setRole(role);

            transaction.commit();

            return user;

        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public void deleteUser(Integer userId) {
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            User user = entityManager.find(User.class, userId);

            if (user == null) {
                throw new IllegalArgumentException(
                        "User with ID " + userId + " was not found."
                );
            }

            entityManager.remove(user);
            transaction.commit();

        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    private boolean nationalIdExists(String nationalId) {
        Long count = entityManager
                .createQuery(
                        """
                        SELECT COUNT(u)
                        FROM User u
                        WHERE u.nationalId = :nationalId
                        """,
                        Long.class
                )
                .setParameter("nationalId", nationalId)
                .getSingleResult();

        return count > 0;
    }

    private void validateUserData(
            String firstName,
            String lastName,
            String nationalId,
            Role role
    ) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException(
                    "First name cannot be empty."
            );
        }

        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException(
                    "Last name cannot be empty."
            );
        }

        if (nationalId == null || nationalId.isBlank()) {
            throw new IllegalArgumentException(
                    "National ID cannot be empty."
            );
        }

        if (role == null) {
            throw new IllegalArgumentException(
                    "Role cannot be null."
            );
        }
    }

    private void rollback(EntityTransaction transaction) {
        if (transaction.isActive()) {
            transaction.rollback();
        }
    }
}