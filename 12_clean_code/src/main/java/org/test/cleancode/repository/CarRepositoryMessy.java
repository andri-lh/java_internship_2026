package org.test.cleancode.repository;

import org.springframework.stereotype.Repository;
import org.test.cleancode.domain.Car;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CarRepositoryMessy {
    public Map<Long, Car> db = new HashMap<>();
    public long seq = 1L;

    public Car s(Car c) {
        if (c.id == null) {
            c.id = seq;
            seq = seq + 1;
        }
        db.put(c.id, c);
        return c;
    }

    public List<Car> f() {
        return new ArrayList<>(db.values());
    }
}
