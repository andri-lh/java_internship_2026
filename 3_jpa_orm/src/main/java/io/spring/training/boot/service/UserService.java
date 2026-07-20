package io.spring.training.boot.service;

import io.spring.training.boot.model.User;
import jakarta.persistence.EntityManager;

import java.util.List;

public class UserService {

    private final EntityManager em;

    public UserService(EntityManager em) {
        this.em = em;
    }

    public User create(User user) {
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
        return user;
    }

    public User findById(long id) {
        return em.find(User.class, id);
    }

    public List<User> findAll() {
        return em
                .createQuery("select u from User u", User.class)
                .getResultList();
    }

    public User update(int id, String username, String role) {

        em.getTransaction().begin();

        User user = em.find(User.class, id);
        if  (user != null) {
            user.setUsername(username);
            user.setRole(role);
        }

        em.getTransaction().commit();
        return user;
    }

    public boolean delete(long id) {
        em.getTransaction().begin();
        User user = em.find(User.class, id);
        if(user == null) {
            em.getTransaction().commit();
            return false;
        }
        em.remove(user);
        em.getTransaction().commit();
        return true;
    }


}
