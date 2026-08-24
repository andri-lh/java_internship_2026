package com.al.lhind.demo.springboot.jpa_practise.repository;

import com.al.lhind.demo.springboot.jpa_practise.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByDepartment(String department);

    List<Employee> findBySalaryGreaterThan(BigDecimal salary);

    List<Employee> findByFirstNameContaining(String text);

    List<Employee> findByDepartmentAndSalaryGreaterThan(String department, BigDecimal salary);

    List<Employee> findAllByOrderBySalaryDesc();

    @Query("SELECT e FROM Employee e WHERE e.hireDate > :date")
    List<Employee> findEmployeeHiredAfterDate(@Param("date") LocalDate date);

    @Query(value = "SELECT * FROM Employee WHERE salary > :salary", nativeQuery = true)
    List<Employee> findEmployeeWithSalaryGreaterThan(@Param("salary") BigDecimal salary);

    Page<Employee> findAll(Pageable pageable);

}
