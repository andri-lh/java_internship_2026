package org.test.cleancode.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.test.cleancode.domain.Car;
import org.test.cleancode.repository.CarRepositoryMessy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CarServiceMessy {
    @Autowired
    private CarRepositoryMessy r;

    public Map<String, Object> register(Map<String, Object> body) {
        Map<String, Object> out = new HashMap<>();
        String owner = null;
        String plate = null;
        String model = null;

        if (body != null) {
            if (body.get("owner") != null) {
                owner = String.valueOf(body.get("owner"));
            } else {
                owner = "";
            }
            if (body.get("plate") != null) {
                plate = String.valueOf(body.get("plate"));
            } else {
                plate = "";
            }
            if (body.get("model") != null) {
                model = String.valueOf(body.get("model"));
            } else {
                model = "";
            }
        } else {
            owner = "";
            plate = "";
            model = "";
        }

        if (owner.trim().isEmpty()) {
            out.put("ok", false);
            out.put("code", 400);
            out.put("message", "owner missing");
            return out;
        } else {
            if (plate.trim().isEmpty()) {
                out.put("ok", false);
                out.put("code", 400);
                out.put("message", "plate missing");
                return out;
            } else {
                List<Car> all = r.f();
                for (int i = 0; i < all.size(); i++) {
                    Car c0 = all.get(i);
                    if (c0.plateNumber != null) {
                        if (c0.plateNumber.equalsIgnoreCase(plate.trim())) {
                            out.put("ok", false);
                            out.put("code", 409);
                            out.put("message", "duplicate plate");
                            return out;
                        }
                    }
                }
            }
        }

        Car c = new Car();
        c.ownerName = owner;
        c.plateNumber = plate.trim();
        c.model = model;
        Car saved = r.s(c);

        out.put("ok", true);
        out.put("code", 200);
        out.put("message", "car registered");
        out.put("car", saved);
        out.put("sizeNow", r.db.size());
        return out;
    }
}
