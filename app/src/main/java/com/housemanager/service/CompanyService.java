package com.housemanager.service;

import com.housemanager.dto.CompanyRegistrationDto;
import com.housemanager.model.Company;
import com.housemanager.model.Role;
import com.housemanager.model.RoleType;
import com.housemanager.model.User;
import com.housemanager.repository.CompanyRepository;
import com.housemanager.repository.RoleRepository;
import com.housemanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void createCompany(CompanyRegistrationDto dto) {

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        if (companyRepository.existsByName(dto.getCompanyName())) {
            throw new RuntimeException("Error: Company name is already taken!");
        }

        if (companyRepository.existsByBulstat(dto.getBulstat())) {
            throw new RuntimeException("Error: Bulstat is already registered!");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        Role companyRole = roleRepository.findByName(RoleType.ROLE_COMPANY)
                .orElseThrow(() -> new RuntimeException("Error: Role 'COMPANY' not found."));

        user.setRoles(Set.of(companyRole));

        Company company = new Company();
        company.setName(dto.getCompanyName());
        company.setBulstat(dto.getBulstat());
        company.setAddress(dto.getAddress());
        company.setContactPerson(dto.getContactPerson());

        company.setUser(user);

        companyRepository.save(company);
    }
}