package io.spring.training.boot.service;

import io.spring.training.boot.model.Fine;
import io.spring.training.boot.model.FineStatus;
import io.spring.training.boot.model.User;
import io.spring.training.boot.model.Vehicle;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.math.BigDecimal;
import java.util.List;

public class FineService {

    private final EntityManager entityManager;

    public FineService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Fine createFine(
            BigDecimal amount,
            String description,
            Integer policeOfficerId,
            Long vehicleId
    ) {
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            User policeOfficer =
                    entityManager.find(User.class, policeOfficerId);

            if (policeOfficer == null) {
                throw new IllegalArgumentException(
                        "Police officer with ID "
                                + policeOfficerId
                                + " was not found."
                );
            }

            Vehicle vehicle =
                    entityManager.find(Vehicle.class, vehicleId);

            if (vehicle == null) {
                throw new IllegalArgumentException(
                        "Vehicle with ID "
                                + vehicleId
                                + " was not found."
                );
            }

            Fine fine = new Fine(
                    amount,
                    description,
                    policeOfficer,
                    vehicle
            );

            entityManager.persist(fine);

            transaction.commit();

            return fine;

        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public Fine findById(Integer fineId) {
        if (fineId == null) {
            throw new IllegalArgumentException(
                    "Fine ID cannot be null."
            );
        }

        Fine fine = entityManager.find(Fine.class, fineId);

        if (fine == null) {
            throw new IllegalArgumentException(
                    "Fine with ID " + fineId + " was not found."
            );
        }

        return fine;
    }

    public List<Fine> findAll() {
        return entityManager
                .createQuery(
                        "SELECT f FROM Fine f ORDER BY f.id",
                        Fine.class
                )
                .getResultList();
    }

    public List<Fine> findByVehicleId(Long vehicleId) {
        if (vehicleId == null) {
            throw new IllegalArgumentException(
                    "Vehicle ID cannot be null."
            );
        }

        return entityManager
                .createQuery(
                        """
                        SELECT f
                        FROM Fine f
                        WHERE f.vehicle.id = :vehicleId
                        ORDER BY f.id
                        """,
                        Fine.class
                )
                .setParameter("vehicleId", vehicleId)
                .getResultList();
    }

    public List<Fine> findByPoliceOfficerId(Integer policeOfficerId) {
        if (policeOfficerId == null) {
            throw new IllegalArgumentException(
                    "Police officer ID cannot be null."
            );
        }

        return entityManager
                .createQuery(
                        """
                        SELECT f
                        FROM Fine f
                        WHERE f.policeOfficer.id = :policeOfficerId
                        ORDER BY f.id
                        """,
                        Fine.class
                )
                .setParameter("policeOfficerId", policeOfficerId)
                .getResultList();
    }

    public Fine payFine(Integer fineId) {
        return changeStatus(
                fineId,
                FineStatus.UNPAID,
                FineStatus.PAID,
                null
        );
    }

    public Fine cancelFine(
            Integer fineId,
            String correctionReason
    ) {
        validateCorrectionReason(correctionReason);

        return changeStatus(
                fineId,
                FineStatus.UNPAID,
                FineStatus.CANCELLED,
                correctionReason
        );
    }

    public Fine requestRefund(
            Integer fineId,
            String correctionReason
    ) {
        validateCorrectionReason(correctionReason);

        return changeStatus(
                fineId,
                FineStatus.PAID,
                FineStatus.REFUND_PENDING,
                correctionReason
        );
    }

    public Fine completeRefund(Integer fineId) {
        return changeStatus(
                fineId,
                FineStatus.REFUND_PENDING,
                FineStatus.REFUNDED,
                null
        );
    }

    private Fine changeStatus(
            Integer fineId,
            FineStatus requiredStatus,
            FineStatus newStatus,
            String correctionReason
    ) {
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            Fine fine = entityManager.find(Fine.class, fineId);

            if (fine == null) {
                throw new IllegalArgumentException(
                        "Fine with ID "
                                + fineId
                                + " was not found."
                );
            }

            if (fine.getStatus() != requiredStatus) {
                throw new IllegalStateException(
                        "Fine must have status "
                                + requiredStatus
                                + " before changing to "
                                + newStatus
                                + "."
                );
            }

            fine.setStatus(newStatus);

            if (correctionReason != null) {
                fine.setCorrectionReason(correctionReason);
            }

            transaction.commit();

            return fine;

        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public void deleteFine(Integer fineId) {
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            Fine fine = entityManager.find(Fine.class, fineId);

            if (fine == null) {
                throw new IllegalArgumentException(
                        "Fine with ID "
                                + fineId
                                + " was not found."
                );
            }

            entityManager.remove(fine);

            transaction.commit();

        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    private void validateCorrectionReason(String correctionReason) {
        if (correctionReason == null || correctionReason.isBlank()) {
            throw new IllegalArgumentException(
                    "Correction reason cannot be empty."
            );
        }
    }

    private void rollback(EntityTransaction transaction) {
        if (transaction.isActive()) {
            transaction.rollback();
        }
    }
}