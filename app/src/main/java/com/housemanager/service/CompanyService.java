package com.housemanager.service;

import com.housemanager.dto.CompanyRegistrationDto;
import com.housemanager.model.*;
import com.housemanager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;

@Service
public class CompanyService {

    @Autowired private CompanyRepository companyRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private BuildingRepository buildingRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private ResidentRepository residentRepository;
    @Autowired private ApartmentRepository apartmentRepository;
    @Autowired private PaymentRepository paymentRepository;

    @Transactional
    public void createCompany(CompanyRegistrationDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) throw new RuntimeException("Username taken");
        if (userRepository.existsByEmail(dto.getEmail())) throw new RuntimeException("Email taken");
        if (companyRepository.existsByName(dto.getCompanyName())) throw new RuntimeException("Company name taken");
        if (companyRepository.existsByBulstat(dto.getBulstat())) throw new RuntimeException("Bulstat taken");

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        Role companyRole = roleRepository.findByName(RoleType.ROLE_COMPANY)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRoles(Set.of(companyRole));

        Company company = getCompany(dto, user);
        companyRepository.save(company);
    }

    private static Company getCompany(CompanyRegistrationDto dto, User user) {
        Company company = new Company();
        company.setName(dto.getCompanyName());
        company.setBulstat(dto.getBulstat());
        company.setAddress(dto.getAddress());
        company.setContactPerson(dto.getContactPerson());
        company.setDefaultTaxPerSqM(dto.getDefaultTaxPerSqM() != null ? dto.getDefaultTaxPerSqM() : 0.0);
        company.setDefaultElevatorTax(dto.getDefaultElevatorTax() != null ? dto.getDefaultElevatorTax() : 0.0);
        company.setDefaultPetTax(dto.getDefaultPetTax() != null ? dto.getDefaultPetTax() : 0.0);
        company.setUser(user);
        return company;
    }

    @Transactional
    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));

        Set<User> usersToDelete = new HashSet<>();

        if (company.getUser() != null) {
            usersToDelete.add(company.getUser());
        }

        List<Employee> employees = employeeRepository.findByCompany(company);
        for (Employee emp : employees) {
            if (emp.getUser() != null) {
                usersToDelete.add(emp.getUser());
            }
        }

        List<Building> buildings = buildingRepository.findByCompany(company);

        for (Building building : buildings) {
            List<Apartment> apartments = apartmentRepository.findByBuilding(building);

            for (Apartment apartment : apartments) {
                if (apartment.getOwner() != null) {
                    usersToDelete.add(apartment.getOwner());
                    apartment.setOwner(null);
                    apartmentRepository.save(apartment);
                }
            }
        }

        for (User user : usersToDelete) {
            List<Payment> userPayments = paymentRepository.findByPayer(user);
            if (!userPayments.isEmpty()) {
                paymentRepository.deleteAll(userPayments);
            }
        }

        for (Building building : buildings) {
            List<Apartment> apartments = apartmentRepository.findByBuilding(building);
            for (Apartment apartment : apartments) {
                List<Payment> aptPayments = paymentRepository.findByApartment(apartment);
                if (!aptPayments.isEmpty()) {
                    paymentRepository.deleteAll(aptPayments);
                }
            }
        }

        paymentRepository.flush();

        for (Building building : buildings) {
            List<Apartment> apartments = apartmentRepository.findByBuilding(building);

            for (Apartment apartment : apartments) {
                List<Resident> residents = residentRepository.findByApartment(apartment);
                residentRepository.deleteAll(residents);
            }
            apartmentRepository.deleteAll(apartments);
        }

        apartmentRepository.flush();

        buildingRepository.deleteAll(buildings);
        buildingRepository.flush();

        employeeRepository.deleteAll(employees);
        employeeRepository.flush();

        companyRepository.delete(company);
        companyRepository.flush();

        for (User user : usersToDelete) {
            if (userRepository.existsById(user.getId())) {
                userRepository.delete(user);
            }
        }

        userRepository.flush();
    }

    @Transactional
    public void updateCompany(Long id, CompanyRegistrationDto dto, Long newUserId) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));

        if (dto.getCompanyName() != null && !dto.getCompanyName().isEmpty()) {
            company.setName(dto.getCompanyName());
        }
        if (dto.getBulstat() != null && !dto.getBulstat().isEmpty()) {
            company.setBulstat(dto.getBulstat());
        }
        if (dto.getContactPerson() != null) {
            company.setContactPerson(dto.getContactPerson());
        }

        if (dto.getDefaultTaxPerSqM() != null) company.setDefaultTaxPerSqM(dto.getDefaultTaxPerSqM());
        if (dto.getDefaultElevatorTax() != null) company.setDefaultElevatorTax(dto.getDefaultElevatorTax());
        if (dto.getDefaultPetTax() != null) company.setDefaultPetTax(dto.getDefaultPetTax());

        if (newUserId != null) {
            User newUser = userRepository.findById(newUserId).orElse(null);

            if (newUser != null) {
                Optional<Employee> promotedEmployeeOpt = employeeRepository.findByUser(newUser);

                if (promotedEmployeeOpt.isPresent() &&
                        promotedEmployeeOpt.get().getCompany().getId().equals(company.getId())) {

                    reassignBuildings(company, promotedEmployeeOpt.get());
                }
                Role companyRole = roleRepository.findByName(RoleType.ROLE_COMPANY)
                        .orElseThrow(() -> new RuntimeException("Error: Role ROLE_COMPANY not found."));

                newUser.getRoles().clear();
                newUser.getRoles().add(companyRole);

                userRepository.save(newUser);

                company.setUser(newUser);
            }
        } else {
            company.setUser(null);
        }

        companyRepository.save(company);
    }

    private void reassignBuildings(Company company, Employee promotedEmployee) {
        List<Building> buildingsToTransfer = buildingRepository.findByEmployee(promotedEmployee);

        if (buildingsToTransfer.isEmpty()) {
            return;
        }

        List<Employee> colleagues = employeeRepository.findByCompany(company);

        Employee targetEmployee = null;
        long minBuildings = Long.MAX_VALUE;

        for (Employee emp : colleagues) {
            if (emp.getId().equals(promotedEmployee.getId())) {
                continue;
            }

            long currentCount = buildingRepository.countByEmployee(emp);
            if (currentCount < minBuildings) {
                minBuildings = currentCount;
                targetEmployee = emp;
            }
        }

        if (targetEmployee != null) {
            for (Building building : buildingsToTransfer) {
                building.setEmployee(targetEmployee);
                buildingRepository.save(building);
            }
        }
    }
}