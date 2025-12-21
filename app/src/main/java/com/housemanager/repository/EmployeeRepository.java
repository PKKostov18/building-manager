package com.housemanager.repository;

import com.housemanager.model.Company;
import com.housemanager.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findAllByCompany(Company company);

    Optional<Employee> findByEmailAndCompany(String email, Company company);

    Optional<Employee> findByUser_Username(String username);
}