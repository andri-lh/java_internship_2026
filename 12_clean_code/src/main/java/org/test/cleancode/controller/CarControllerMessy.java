package org.test.cleancode.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.test.cleancode.service.CarServiceMessy;

import java.util.Map;

@RestController
@RequestMapping("/cars")
public class CarControllerMessy {
    @Autowired
    private CarServiceMessy s;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> reg(@RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> result = s.register(body);
        Object code = result.get("code");
        if (code instanceof Integer) {
            return ResponseEntity.status((Integer) code).body(result);
        }
        return ResponseEntity.ok(result);
    }
}
