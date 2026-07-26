package io.spring.training.boot.service;

import io.spring.training.boot.model.User;
import io.spring.training.boot.model.Vehicle;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class VehicleService {

    private final EntityManager entityManager;

    public VehicleService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Vehicle createVehicle(String plateNumber,
                                 String vin,
                                 Integer ownerId) {

        validateVehicleData(plateNumber, vin, ownerId);

        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            if (plateNumberExists(plateNumber)) {
                throw new IllegalArgumentException(
                        "A vehicle with this plate number already exists."
                );
            }

            if (vinExists(vin)) {
                throw new IllegalArgumentException(
                        "A vehicle with this VIN already exists."
                );
            }

            User owner = entityManager.find(User.class, ownerId);

            if (owner == null) {
                throw new IllegalArgumentException(
                        "Owner not found."
                );
            }

            Vehicle vehicle = new Vehicle(plateNumber, vin);
            vehicle.setOwner(owner);

            entityManager.persist(vehicle);

            transaction.commit();

            return vehicle;

        } catch (RuntimeException e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw e;
        }
    }

    public Vehicle findById(Long vehicleId) {

        Vehicle vehicle = entityManager.find(Vehicle.class, vehicleId);

        if (vehicle == null) {
            throw new IllegalArgumentException(
                    "Vehicle not found."
            );
        }

        return vehicle;
    }

    public Vehicle findByPlateNumber(String plateNumber) {

        return entityManager.createQuery(
                        "SELECT v FROM Vehicle v WHERE v.plateNumber = :plate",
                        Vehicle.class)
                .setParameter("plate", plateNumber)
                .getResultStream()
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Vehicle not found."));
    }

    public Vehicle findByVin(String vin) {

        return entityManager.createQuery(
                        "SELECT v FROM Vehicle v WHERE v.vin = :vin",
                        Vehicle.class)
                .setParameter("vin", vin)
                .getResultStream()
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Vehicle not found."));
    }

    public List<Vehicle> findAll() {

        return entityManager.createQuery(
                "SELECT v FROM Vehicle v ORDER BY v.id",
                Vehicle.class
        ).getResultList();
    }

    public List<Vehicle> findByOwner(Integer ownerId) {

        return entityManager.createQuery(
                        "SELECT v FROM Vehicle v WHERE v.owner.id = :ownerId",
                        Vehicle.class)
                .setParameter("ownerId", ownerId)
                .getResultList();
    }

    public Vehicle updateVehicle(Long vehicleId,
                                 String plateNumber,
                                 String vin,
                                 Integer ownerId) {

        validateVehicleData(plateNumber, vin, ownerId);

        EntityTransaction transaction = entityManager.getTransaction();

        try {

            transaction.begin();

            Vehicle vehicle = findById(vehicleId);

            if (!vehicle.getPlateNumber().equals(plateNumber)
                    && plateNumberExists(plateNumber)) {
                throw new IllegalArgumentException(
                        "Plate number already exists."
                );
            }

            if (!vehicle.getVin().equals(vin)
                    && vinExists(vin)) {
                throw new IllegalArgumentException(
                        "VIN already exists."
                );
            }

            User owner = entityManager.find(User.class, ownerId);

            if (owner == null) {
                throw new IllegalArgumentException(
                        "Owner not found."
                );
            }

            vehicle.setPlateNumber(plateNumber);
            vehicle.setVin(vin);
            vehicle.setOwner(owner);

            transaction.commit();

            return vehicle;

        } catch (RuntimeException e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw e;
        }
    }

    public void deleteVehicle(Long vehicleId) {

        EntityTransaction transaction = entityManager.getTransaction();

        try {

            transaction.begin();

            Vehicle vehicle = findById(vehicleId);

            entityManager.remove(vehicle);

            transaction.commit();

        } catch (RuntimeException e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw e;
        }
    }

    private boolean plateNumberExists(String plateNumber) {

        Long count = entityManager.createQuery(
                        "SELECT COUNT(v) FROM Vehicle v WHERE v.plateNumber = :plate",
                        Long.class)
                .setParameter("plate", plateNumber)
                .getSingleResult();

        return count > 0;
    }

    private boolean vinExists(String vin) {

        Long count = entityManager.createQuery(
                        "SELECT COUNT(v) FROM Vehicle v WHERE v.vin = :vin",
                        Long.class)
                .setParameter("vin", vin)
                .getSingleResult();

        return count > 0;
    }

    private void validateVehicleData(String plateNumber,
                                     String vin,
                                     Integer ownerId) {

        if (plateNumber == null || plateNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Plate number cannot be empty."
            );
        }

        if (vin == null || vin.isBlank()) {
            throw new IllegalArgumentException(
                    "VIN cannot be empty."
            );
        }

        if (ownerId == null) {
            throw new IllegalArgumentException(
                    "Owner ID cannot be null."
            );
        }
    }
}