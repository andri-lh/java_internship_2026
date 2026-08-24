package com.al.lhind.demo.springboot.jpa_practise;

import com.al.lhind.demo.springboot.jpa_practise.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Scanner;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.al.lhind.demo.springboot.jpa_practise.entity.Employee;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(EmployeeRepository repository) {
        return args -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\n===== Employee Management =====");
                System.out.println("1 - Find employees by department");
                System.out.println("2 - Find employees with salary greater than");
                System.out.println("3 - Find employees whose last name contains");
                System.out.println("4 - Find employees hired after a date");
                System.out.println("5 - Execute native salary query");
                System.out.println("6 - Show first page (5 employees)");
                System.out.println("7 - Show employees sorted by salary (descending)");
                System.out.println("0 - Exit");
                System.out.print("Choose an option: ");
                int option = Integer.parseInt(scanner.nextLine());
                switch (option) {
                    case 1 -> {
                        System.out.print("Department: ");
                        String department = scanner.nextLine();
                        repository.findByDepartment(department).forEach(System.out::println);
                    }
                    case 2 -> {
                        System.out.print("Minimum salary: ");
                        BigDecimal salary = new BigDecimal(scanner.nextLine());
                        repository.findBySalaryGreaterThan(salary).forEach(System.out::println);
                    }
                    case 3 -> {
                        System.out.print("First name contains: ");
                        String text = scanner.nextLine();
                        repository.findByFirstNameContaining(text).forEach(System.out::println);
                    }
                    case 4 -> {
                        System.out.print("Hire date (yyyy-MM-dd): ");
                        LocalDate hireDate = LocalDate.parse(scanner.nextLine());
                        repository.findEmployeeHiredAfterDate(hireDate).forEach(System.out::println);
                    }
                    case 5 -> {
                        System.out.print("Minimum salary: ");
                        BigDecimal salary = new BigDecimal(scanner.nextLine());
                        repository.findByDepartmentAndSalaryGreaterThan("Engineering", salary).forEach(System.out::println);

                    }
                    case 6 -> {
                        System.out.println("First page (5 employees):");
                        Page<Employee> page = repository.findAll(PageRequest.of(0, 5));
                        page.getContent().forEach(System.out::println);
                    }
                    case 7 -> {
                        repository.findAllByOrderBySalaryDesc().forEach(System.out::println);
                    }
                    case 0 -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid option.");
                }
            }
        };
    }
}