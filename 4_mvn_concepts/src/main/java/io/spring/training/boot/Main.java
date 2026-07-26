package io.spring.training.boot;

import io.spring.training.boot.model.Fine;
import io.spring.training.boot.model.Role;
import io.spring.training.boot.model.User;
import io.spring.training.boot.model.Vehicle;
import io.spring.training.boot.service.FineService;
import io.spring.training.boot.service.UserService;
import io.spring.training.boot.service.VehicleService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.math.BigDecimal;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("eTicketPU");

        EntityManager entityManager =
                entityManagerFactory.createEntityManager();

        try {
            UserService userService =
                    new UserService(entityManager);

            VehicleService vehicleService =
                    new VehicleService(entityManager);

            FineService fineService =
                    new FineService(entityManager);

            // 1. Create a normal user
            User vehicleOwner = userService.createUser(
                    "John",
                    "Smith",
                    "NID-1001",
                    Role.USER
            );

            // 2. Create a police officer
            User policeOfficer = userService.createUser(
                    "Sarah",
                    "Johnson",
                    "NID-2001",
                    Role.POLICE
            );

            // 3. Register a vehicle for the normal user
            Vehicle vehicle = vehicleService.createVehicle(
                    "ABC-123",
                    "1HGCM82633A123456",
                    vehicleOwner.getId()
            );

            // 4. Police officer issues a fine
            Fine fine = fineService.createFine(
                    new BigDecimal("150.00"),
                    "Speeding above the legal limit",
                    policeOfficer.getId(),
                    vehicle.getId()
            );

            System.out.println("Created fine:");
            System.out.println(fine);

            // 5. Display all fines
            List<Fine> fines = fineService.findAll();

            System.out.println("\nAll fines:");

            for (Fine currentFine : fines) {
                System.out.println(currentFine);
            }

            // 6. Pay the fine
            Fine paidFine = fineService.payFine(fine.getId());

            System.out.println("\nPaid fine:");
            System.out.println(paidFine);

            // Example refund workflow:
            /*
            Fine refundPending = fineService.requestRefund(
                    paidFine.getId(),
                    "Fine was issued to the wrong vehicle."
            );

            System.out.println("\nRefund pending:");
            System.out.println(refundPending);

            Fine refundedFine =
                    fineService.completeRefund(refundPending.getId());

            System.out.println("\nRefunded fine:");
            System.out.println(refundedFine);
            */

        } catch (Exception exception) {
            System.err.println(
                    "Application error: " + exception.getMessage()
            );

            exception.printStackTrace();

        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }

            if (entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
            }
        }
    }
}