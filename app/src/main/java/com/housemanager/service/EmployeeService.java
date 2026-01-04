package com.housemanager.service;

import com.housemanager.dto.EmployeeRegistrationDto;
import com.housemanager.model.*;
import com.housemanager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class EmployeeService {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private BuildingRepository buildingRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public List<Employee> findEmployeesByCompanyUser(String companyUsername) {
        Company company = companyRepository.findByUser_Username(companyUsername)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        return employeeRepository.findAllByCompany(company);
    }

    @Transactional
    public void createEmployee(EmployeeRegistrationDto dto, String companyOwnerUsername) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        Company company = companyRepository.findByUser_Username(companyOwnerUsername)
                .orElseThrow(() -> new RuntimeException("Current user is not a company owner"));

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRoles(Set.of(roleRepository.findByName(RoleType.ROLE_EMPLOYEE).get()));
        userRepository.save(user);

        Employee employee = new Employee();
        employee.setName(dto.getName());
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setEmail(dto.getEmail());
        employee.setCompany(company);
        employee.setUser(user);

        employee = employeeRepository.save(employee);

        List<Building> companyBuildings = buildingRepository.findAllByCompany(company);

        for (Building b : companyBuildings) {
            if (b.getEmployee() == null) {
                b.setEmployee(employee);
                buildingRepository.save(b);
            }
        }
    }

    public Employee findCurrentEmployee(String username) {
        return employeeRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Employee profile not found for user: " + username));
    }
}