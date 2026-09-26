package org.test.cleancode.domain;

public class Car {
    public Long id;
    public String ownerName;
    public String plateNumber;
    public String model;

    public Car() {
    }

    public Car(Long id, String ownerName, String plateNumber, String model) {
        this.id = id;
        this.ownerName = ownerName;
        this.plateNumber = plateNumber;
        this.model = model;
    }
}
