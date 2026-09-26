package org.test.cleancode.repository;

import org.test.cleancode.domain.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findByEmail(String email);
}
