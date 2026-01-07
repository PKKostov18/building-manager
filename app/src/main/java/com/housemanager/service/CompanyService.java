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
import java.util.stream.Collectors;
import java.util.HashSet;

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

        Company company = new Company();
        company.setName(dto.getCompanyName());
        company.setBulstat(dto.getBulstat());
        company.setAddress(dto.getAddress());
        company.setContactPerson(dto.getContactPerson());
        company.setDefaultTaxPerSqM(dto.getDefaultTaxPerSqM() != null ? dto.getDefaultTaxPerSqM() : 0.0);
        company.setDefaultElevatorTax(dto.getDefaultElevatorTax() != null ? dto.getDefaultElevatorTax() : 0.0);
        company.setDefaultPetTax(dto.getDefaultPetTax() != null ? dto.getDefaultPetTax() : 0.0);
        company.setUser(user);
        companyRepository.save(company);
    }

    @Transactional
    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));

        // 1. СЪБИРАМЕ ВСИЧКИ ПОТРЕБИТЕЛИ И РАЗКАЧАМЕ СОБСТВЕНИЦИТЕ (CRITICAL FIX)
        Set<User> usersToDelete = new HashSet<>();

        // А. Company Admin
        if (company.getUser() != null) {
            usersToDelete.add(company.getUser());
        }

        // Б. Employee Users
        List<Employee> employees = employeeRepository.findByCompany(company);
        for (Employee emp : employees) {
            if (emp.getUser() != null) {
                usersToDelete.add(emp.getUser());
            }
        }

        // В. Apartment Owners - ТУК Е ОСНОВНАТА КОРЕКЦИЯ
        List<Building> buildings = buildingRepository.findByCompany(company);

        // Събираме всички апартаменти от всички сгради наведнъж (или в цикъл)
        for (Building building : buildings) {
            List<Apartment> apartments = apartmentRepository.findByBuilding(building);

            for (Apartment apartment : apartments) {
                if (apartment.getOwner() != null) {
                    // Добавяме в списъка за триене
                    usersToDelete.add(apartment.getOwner());

                    // ВАЖНО: Премахваме връзката веднага!
                    // Това предотвратява грешката, дори ако Hibernate размести реда на заявките
                    apartment.setOwner(null);
                    apartmentRepository.save(apartment);
                }
            }
        }

        // 2. ПОЧИСТВАНЕ НА ПЛАЩАНИЯТА
        // 2.1. Плащания на потребителите
        for (User user : usersToDelete) {
            List<Payment> userPayments = paymentRepository.findByPayer(user);
            if (!userPayments.isEmpty()) {
                paymentRepository.deleteAll(userPayments);
            }
        }

        // 2.2. Плащания на апартаментите
        for (Building building : buildings) {
            List<Apartment> apartments = apartmentRepository.findByBuilding(building);
            for (Apartment apartment : apartments) {
                List<Payment> aptPayments = paymentRepository.findByApartment(apartment);
                if (!aptPayments.isEmpty()) {
                    paymentRepository.deleteAll(aptPayments);
                }
            }
        }

        // Изчистваме плащанията от базата веднага
        paymentRepository.flush();

        // 3. ТРИЕМ СТРУКТУРАТА (ОТДОЛУ-НАГОРЕ)
        for (Building building : buildings) {
            List<Apartment> apartments = apartmentRepository.findByBuilding(building);

            for (Apartment apartment : apartments) {
                // Трием жителите
                List<Resident> residents = residentRepository.findByApartment(apartment);
                residentRepository.deleteAll(residents);
            }
            // Трием апартаментите
            apartmentRepository.deleteAll(apartments);
        }

        // ВАЖНО: Казваме на базата, че апартаментите ги няма ВЕЧЕ.
        // Без този flush, SQL сървърът може още да пази апартаментите, когато стигнем до триенето на User.
        apartmentRepository.flush();

        // Трием сградите
        buildingRepository.deleteAll(buildings);
        buildingRepository.flush();

        // 4. ТРИЕМ СЛУЖИТЕЛИТЕ И КОМПАНИЯТА
        // Тъй като вече няма апартаменти, които да сочат към User-ите, Employees могат да се трият безопасно
        employeeRepository.deleteAll(employees);
        employeeRepository.flush();

        companyRepository.delete(company);
        companyRepository.flush();

        // 5. ТРИЕМ ПОТРЕБИТЕЛИТЕ (НАКРАЯ)
        for (User user : usersToDelete) {
            // Проверяваме отново дали съществува, защото Cascade Delete от Employee или Company може вече да го е изтрил
            if (userRepository.existsById(user.getId())) {
                userRepository.delete(user);
            }
        }

        userRepository.flush();
    }
    @Transactional
    public void updateCompany(Long id, CompanyRegistrationDto dto) {
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

        companyRepository.save(company);
    }
}