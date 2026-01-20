package com.housemanager.repository;

import com.housemanager.model.Company;
import com.housemanager.model.Employee;
import com.housemanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findAllByCompany(Company company);
    long countByCompany(Company company);
    Optional<Employee> findByUser_Username(String username);
    List<Employee> findByCompany(Company company);
    Optional<Employee> findByUser(User user);
    List<Employee> findByCompanyOrderByNameAsc(Company company);
    @Query("SELECT e FROM Employee e WHERE e.company = :company ORDER BY SIZE(e.buildings) DESC")
    List<Employee> findByCompanyOrderByBuildingsCount(@Param("company") Company company);
    List<Employee> findByCompanyAndNameContainingIgnoreCase(Company company, String name);
}