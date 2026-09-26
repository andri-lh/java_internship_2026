package org.test.cleancode.repository;

import org.springframework.stereotype.Repository;
import org.test.cleancode.domain.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
// Spring beans use singleton scope by default.
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> usersById = new HashMap<>();
    private long nextId = 1L;

    @Override
    public User save(User user) {
        Long userId = user.getId();
        if (userId == null) {
            userId = nextId++;
        }
        User savedUser = new User(userId, user.getName(), user.getEmail());
        usersById.put(userId, savedUser);
        return savedUser;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return usersById.values()
                .stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
